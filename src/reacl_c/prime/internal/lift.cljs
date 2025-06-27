(ns ^:no-doc reacl-c.prime.internal.lift
  (:require [reacl-c.core :as c :include-macros true]
            [reacl-c.dom :include-macros true]
            [reacl-c.main.react :as main]
            [reacl-c.main :as c-main]
            [reacl-c.interop.react :as interop]))

(defn embed-event-attrs [embed-handler event? attrs]
  (->> attrs
       (reduce (fn [res [k v]]
                 (if (event? k)
                   (assoc! res k (embed-handler v))
                   res))
               (transient attrs))
       (persistent!)))

(defn default-is-event? [k]
  ;; TODO: something more sophisticated; from reacl.dom dom-base/event-attribute?
  (.startsWith (name k) "on"))

(defn- fragment* [items]
  ;; Note: the fragment works around a react warning of "children need a key" for now.
  ;; But it also reduces the overhead, though.
  (cond
    (empty? items) nil
    (empty? (rest items)) (first items)
    ;; prevents having keys in embedded children - needs a different solution when that's needed.
    :else (c/fragment items)))

(defn child-wrapper [attrs children f]
  (main/with-embed
   (fn [embed-item embed-event-handler]
     (f (cond-> attrs
          ;; Note: leaving a :children property untouched if children list is empty (used in data-table)
          (not-empty children) (assoc :children (embed-item (fragment* children))))
        embed-item embed-event-handler))))

(defn react-item [class attrs]
  (interop/lift class (clj->js attrs)))
