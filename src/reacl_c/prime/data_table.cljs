(ns reacl-c.prime.data-table
  (:require
   [active.clojure.lens :as lens]
   [reacl-c.prime.internal.lift :as lift :include-macros true]
   [reacl-c.prime.internal.util :as util]
   [reacl-c.core :as c :include-macros true]
   [reacl-c.interop.react :as interop]
   ["primereact/datatable" :as dt]
   ["primereact/column" :as co]
   ["primereact/columngroup" :as cg]
   ["primereact/row" :as ro]
   ["react" :as react]))

(defrecord ^:private Column [attrs])

(defn column [attrs]
  (Column. attrs))

(defn- column-elem [^Column c embed lift-events]
  (react/createElement co/Column
                       (-> (:attrs c)
                           (util/opt-update :body util/item-or-fn embed)
                           (util/opt-update :editor util/item-or-fn embed)
                           (util/opt-update :filterApply util/item-or-fn embed)
                           (util/opt-update :filterElement util/item-or-fn embed)
                           (util/opt-update :filterFooter util/item-or-fn embed)
                           (util/opt-update :filterHeader util/item-or-fn embed)
                           (util/opt-update :footer util/item-or-fn embed)
                           (util/opt-update :header util/item-or-fn embed)
                           (lift-events)
                           (clj->js))))

(defn- column-elems [columns embed lift-events]
  (mapv (fn [c]
          (column-elem c embed lift-events))
        columns))

(defn- dom-like-args [child-pred attrs children]
  (if (child-pred attrs)
    [{} (cons attrs children)]
    [attrs children]))

(defrecord ^:private ColumnGroup [attrs children])

(defrecord ^:private Row [attrs children])

(defn column-group [attrs & children]
  ;; column-groups may contain rows.
  (let [[attrs children] (dom-like-args #(instance? Row %) attrs children)]
    (ColumnGroup. attrs children)))

(defn row [attrs & children]
  ;; rows may contain columns
  (let [[attrs children] (dom-like-args #(instance? Column %) attrs children)]
    (Row. attrs children)))

(defn- embed-row [^Row c embed lift-events]
  ;; rows may contain columns
  (apply react/createElement ro/Row
         (-> (:attrs c)
             (clj->js))
         (column-elems (:children c) embed lift-events)))

(defn- embed-column-group [^ColumnGroup c embed lift-events]
  ;; column-groups may contain rows.
  (apply react/createElement cg/ColumnGroup
         (-> (:attrs c)
             (clj->js))
         (mapv #(embed-row % embed lift-events) (:children c))))

(defn- virtual-scroller-attrs [attrs embed lift-events]
  ;; Note: if we add the VirtualScroller component, this fn could be shared with it.
  (-> attrs
      (util/opt-update :itemTemplate util/item-or-fn embed)
      (util/opt-update :contentTemplate util/item-or-fn embed)
      (util/opt-update :loadingTemplate util/item-or-fn embed)
      (lift-events)
      (clj->js)))

(c/defn-effect ^:private call-handler [f ev]
  (f ev))

(defn- paginator-template-attrs [attrs embed]
  ;; Note: if we add the Paginator component, this fn could be shared with it.
  (let [lift-event-handler
        (fn [f]
          ;; turn a react event handler f, into a reacl-c event handler.
          (fn [_ ev]
            (c/return :action (call-handler f ev))))
        lift-element
        (fn [elem]
          (interop/element elem))
        embed* (fn [f]
                 (fn [options]
                   (embed (f (-> options
                                 (util/js-update "element" lift-element)
                                 (util/js-update "onClick" lift-event-handler))))))]
    (-> attrs
        ;; :layout is a string with the component names to define what's visible and the order.
        (util/opt-update :FirstPageLink embed*)
        (util/opt-update :PrevPageLink embed*)
        (util/opt-update :PageLinks embed*)
        (util/opt-update :NextPageLink embed*)
        (util/opt-update :LastPageLink embed*)
        (util/opt-update :RowsPerPageDropdown embed*)
        (util/opt-update :JumpToPageInput embed*)
        (util/opt-update :CurrentPageReport embed*)
        (clj->js))))

(def default-paginator
  ;; Note: the default paginatorTemplate is not the same as no paginatorTemplate :-/
  (let [dflt (fn [options]
               (.-element options))]
    {:FirstPageLink dflt
     :PrevPageLink dflt
     :PageLinks dflt
     :NextPageLink dflt
     :LastPageLink dflt
     :RowsPerPageDropdown dflt
     :JumpToPageInput dflt
     :CurrentPageReport dflt}))

(lift/def-react-container ^:private base dt/DataTable
  (fn [attrs embed embed-handler]
    (let [lift-events (partial lift/embed-event-attrs embed-handler lift/default-is-event?)]
      (-> attrs
          (util/opt-update :children column-elems embed lift-events)
          (util/opt-update :emptyMessage util/item-or-fn embed)
          (util/opt-update :footerColumnGroup embed-column-group embed lift-events)
          (util/opt-update :headerColumnGroup embed-column-group embed lift-events)
          (util/opt-update :paginatorLeft embed)
          (util/opt-update :paginatorRight embed)
          (util/opt-update :paginatorTemplate paginator-template-attrs embed)
          (util/opt-update :virtualScrollerOptions virtual-scroller-attrs embed lift-events)
          (util/opt-update :rowGroupFooterTemplate util/item-or-fn embed)
          (util/opt-update :rowGroupHeaderTemplate util/item-or-fn embed)
          (lift-events)))))

;; Note: Columns as React elements are just a fancy way to pass
;; parameters via JSX; No need to have them as reacl-c items. For the
;; sake of refential transparency we use a simple record, and create
;; elements on render.

(c/defn-item raw [attrs & columns]
  (let [[attrs columns] (dom-like-args #(instance? Column %) attrs columns)]
    (assert (every? #(instance? Column %) columns))
    (base (assoc attrs :children columns))))

(defn cell-editor [lens input-item]
  (fn [options]
    ;; options: .-editorCallback .-field; -.rowData .-rowIndex .-value
    ;; Note: the .-field in the options is unfortunately already translated to js (keyword=>string)
    ;; Which is why this wrapper needs the lens (again).
    (c/focus (lens/>> (lens/at-index (.-rowIndex options))
                      lens)
             input-item)))

(c/defn-item input "Uses the state as the value to display in the data table." [attrs & columns]
  (assert (not (contains? attrs :value)))
  (c/with-state-as value
    (apply raw (assoc attrs :value value)
           columns)))
