(ns reacl-c.prime.panel
  (:require
   [reacl-c.prime.internal.lift :as lift :include-macros true]
   [reacl-c.prime.internal.util :as util]
   [reacl-c.core :as c :include-macros true]
   ["primereact/panel" :as panel]))

(lift/def-react-container raw panel/Panel
  (fn [attrs embed embed-handler]
    (-> attrs
        (util/opt-update :header embed)
        (util/opt-update :footer embed)
        (util/opt-update :icons util/item-or-fn embed)
        (util/opt-update :headerTemplate util/item-or-fn embed)
        (util/opt-update :footerTemplate util/item-or-fn embed)
        (lift/embed-event-attrs embed-handler lift/default-is-event?))))

;; Methods

(c/defn-effect collapse! [panel-ref]
  (.collapse (c/deref panel-ref)))

(c/defn-effect exapnd! [panel-ref]
  (.expand (c/deref panel-ref)))

(c/defn-effect toggle! [panel-ref]
  (.toggle (c/deref panel-ref)))
