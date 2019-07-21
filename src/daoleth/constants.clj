(ns daoleth.constants)

(def initial-state {
                       :objects {:wall \# :door \o :floor \.}
                       :filename "unknown"
                       :map nil
                       :size [20 20]
                       :saved? false
                       :painting-object :floor
                       })

(defn create-initial-map [state]
  (swap! state assoc :map (vec (replicate (apply * (:size @state)) \.))))
