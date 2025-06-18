(ns reacl-c.prime.data-table
  (:require
   [reacl-c.prime.internal.lift :as lift :include-macros true]
   [reacl-c.prime.internal.util :as util]
   [reacl-c.core :as c :include-macros true]
   ["primereact/datatable" :as dt]
   ["primereact/column" :as co]
   ["react" :as react]))

(lift/def-react-container ^:private base dt/DataTable
  (fn [attrs embed]
    (-> attrs
        (util/opt-update :emptyMessage util/item-or-fn embed)
        ;; TODO: :footerColumnGroup and :headerColumnGroup have to be made from ColumnGroup, Column and Row components - that's something special.
        ;; TODO: paginatorDropdownAppendTo ?
        (util/opt-update :paginatorLeft embed)
        (util/opt-update :paginatorRight embed)
        ;; TODO: paginatorTemplate ?
        (util/opt-update :rowGroupFooterTemplate util/item-or-fn embed)
        (util/opt-update :rowGroupHeaderTemplate util/item-or-fn embed))))

;; Note: Columns as React elements are just a fancy way to pass
;; parameters via JSX; No need to have them as reacl-c items. For the
;; sake of refential transparency we use a simple record, and create
;; elements on render.

(defrecord ^:private Column [attrs])

(defn column [attrs]
  (Column. attrs))

(defn- dom-like-args [attrs columns]
  (if (instance? Column attrs)
    [{} (cons attrs columns)]
    [attrs columns]))

(c/defn-item raw [attrs & columns]
  (let [[attrs columns] (dom-like-args attrs columns)]
    (assert (not (contains? attrs :children)))
    (base (assoc attrs :children (mapv (fn [^Column c]
                                         (react/createElement co/Column (clj->js (:attrs c))))
                                       columns)))))
