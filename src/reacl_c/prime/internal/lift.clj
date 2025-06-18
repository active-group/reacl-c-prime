(ns ^:no-doc reacl-c.prime.internal.lift
  (:require [reacl-c.core :as c]
            [reacl-c.dom :as dom]))

(defmacro def-react [name class]
  ;; Note: for dom-like classes, with attrs containing event-handlers for example.
  `(dom/defn-dom ~name :static [attrs1#]
     (events-wrapper attrs1#
                     default-is-event?
                     (fn [attrs2#]
                       (react-item ~class attrs2#)))))

(defmacro def-react-container [name class & [mod-attrs]]
  ;; Note: for dom-like container classes, with attrs containing event-handlers and child items for example.
  ;; Note: special attrs that can contain items also make it a container.
  ;; mod-attrs: (fn [attrs embed-item] ...attrs)
  `(dom/defn-dom ~name [attrs1# & children#]
     (events-wrapper
      attrs1#
      default-is-event?
      (fn [attrs2#]
        (child-wrapper attrs2# children# (or ~mod-attrs identity)
                       (fn [attrs3#]
                         (react-item ~class attrs3#)))))))
