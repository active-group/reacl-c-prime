(ns ^:no-doc reacl-c.prime.internal.lift
  (:require [reacl-c.core :as c]
            [reacl-c.dom :as dom]))

(defmacro def-react [name class]
  ;; Note: for dom-like classes, with attrs containing event-handlers for example.
  `(dom/defn-dom ~name :static [attrs1#]
     (child-wrapper attrs1# nil
                    (fn [attrs2# embed-item# embed-handler#]
                      (react-item ~class (embed-event-attrs attrs2# embed-handler# default-is-event?))))))

(defmacro def-react-container [name class & [mod-attrs]]
  ;; Note: for dom-like container classes, with attrs containing event-handlers and child items for example.
  ;; Note: special attrs that can contain items also make it a container.
  ;; mod-attrs: (fn [attrs embed-item embed-event-handler] ...attrs); if given, event-handlers are not automatically lifted.
  `(dom/defn-dom ~name [attrs1# & children#]
     (child-wrapper attrs1# children#
                    (fn [attrs2# embed-item# embed-handler#]
                      (react-item ~class (if-let [g# ~mod-attrs]
                                           (g# attrs2# embed-item# embed-handler#)
                                           (embed-event-attrs attrs2# embed-handler# default-is-event?)))))))
