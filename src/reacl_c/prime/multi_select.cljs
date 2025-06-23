(ns reacl-c.prime.multi-select
  (:require
   [reacl-c.prime.internal.lift :as lift :include-macros true]
   [reacl-c.prime.internal.util :as util]
   [reacl-c.core :as c :include-macros true]
   [schema.core :as s]
   ["primereact/multiselect" :as ms]))

(defrecord ^:private Option [label value])

(defn option "Use this for complex data. Simple strings can be used directly as options."
  [value label]
  (Option. label value))

(defn- to-options [v]
  (map (fn [x]
         (if (instance? Option x)
           #js {:value (:value x)
                :label (:label x)}
           x))
       v))

(defn- fixup-value [options value]
  ;; Note: this tries to map the selected values that are = to an
  ;; option value, to the option value (so that they are identical for
  ;; JS code; otherwise users would have to be careful to instantiate
  ;; option lists statically.
  (let [m (->> options
               (map (fn [o]
                      (if (instance? Option o)
                        [(:value o) (:value o)]
                        [o o])))
               (into {}))]
    (map (fn [v]
           (get m v v))
         value)))

(lift/def-react-container raw ms/MultiSelect
  (fn [attrs embed lift-events]
    (-> attrs
        ;; TODO: embed: emptyFilterMessage, filterTemplate, itemTemplate, optionGroupTemplate, panelFooterTemplate, panelHeaderTemplate, selectedItemTemplate
        ;; TODO: virtualScrollerOptions
        ;; TODO: inputRef?
        (util/opt-update :value (comp to-array (partial fixup-value (:options attrs))))
        (util/opt-update :options (comp to-array to-options)))))

(defn- return-value [_value ev]
  (c/return :state (array-seq (.-value ev))))

(c/defn-item input :- s/Any [& [attrs]]
  (c/with-state-as v
    (raw (assoc attrs
                :value v
                :onChange return-value))))
