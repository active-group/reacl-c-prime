(ns ^:no-doc reacl-c.prime.internal.lift
  (:require [reacl-c.core :as c :include-macros true]
            [reacl-c.dom :include-macros true]
            [reacl-c.main.react :as main]
            [reacl-c.main :as c-main]
            [reacl-c.interop.react :as interop]))

(defn events-wrapper [attrs event? f]
  (c/with-async
    (fn [a]
      (let [lift-events (fn [attrs] ;; maybe optionally take event? pred override.
                          (->> attrs
                               (reduce (fn [res [k v]]
                                         (if (event? k)
                                           (assoc! res k (fn [ev]
                                                           (a (fn [state]
                                                                (v state ev)))))
                                           res))
                                       (transient attrs))
                               (persistent!)))]
        (f (lift-events attrs)
           lift-events)))))

(defn default-is-event? [k]
  ;; TODO: something more sophisticated; from reacl.dom dom-base/event-attribute?
  (.startsWith (name k) "on"))

(defn embed* [item state return!]
  (cond
    (nil? item) nil
    (string? item) item
    :else
    (main/embed
     ;; Note: the fragment works around a react warning of "children need a key" for now.
     ;; It also reduces the overhead, though.
     item
     {:state state
      :set-state! (fn [state]
                    (return! (fn [_state]
                               (c/return :state state))))
      ;; :key ?
      ;; :ref ?
      :handle-action! (fn [a]
                        (return! (fn [_state]
                                   (c/return :action a))))})))

(defn child-wrapper-n "Calls f with embed 'embed' function, that creates react elements for child items." [f]
  ;; we might need different versions of this depending on the type of component or usage patterns.
  (c/dynamic
   (fn [state]
     (c/with-async
       (fn [return!]
         (f (fn [item]
              (embed* item state return!))))))))

(defn- fragment* [items]
  ;; Note: the fragment works around a react warning of "children need a key" for now.
  ;; But it also reduces the overhead, though.
  (cond
    (empty? items) nil
    (empty? (rest items)) (first items)
    ;; prevents having keys in embedded children - needs a different solution when that's needed.
    :else (c/fragment items)))

(defn child-wrapper [attrs children mod-attrs lift-event-attrs f]
  (child-wrapper-n
   (fn [embed]
     (f (cond-> attrs
          (some? mod-attrs) (mod-attrs embed lift-event-attrs)
          ;; Note: leaving a :children property untouched if children list is empty (used in data-table)
          (not-empty children) (assoc :children (embed (fragment* children))))))))

(defn react-item [class attrs]
  (interop/lift class (clj->js attrs)))
