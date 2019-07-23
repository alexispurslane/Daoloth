(ns daoleth.constants)

(def initial-state {:objects {:wall \# :door \o :floor \.}
                    :filename "unknown"
                    :map nil
                    :size [20 20]
                    :saved? false
                    :mode :paintbrush
                    :painting-object :floor})

(defn create-initial-map [state]
  (swap! state assoc :map (vec (replicate (second (:size @state))
                                          (vec (replicate (first (:size @state)) \.))))))

(defn get-state-from-file [filename]
  (->> filename slurp read-string))

(defn write-state-to-file [state]
  (spit (:filename state) (with-out-str
                            (-> state
                                (select-keys [:map :size :filename :objects])
                                pr))))

(defn +p [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn map-effect [type map [pos-x pos-y] val]
  (case type
    :paintbrush
    (update-in map [pos-y pos-x] (fn [o] val))
    :bucketfill
    (let [start-val (get-in map [pos-y pos-x])]
      (reduce (fn [acc x]
                (let [npos (+p x [pos-y pos-x]) cur-val (get-in map npos)]
                  (if (= cur-val start-val)
                    (map-effect :bucketfill map npos val))))
              (update-in map [pos-y pos-x] (fn [o] val))
              [[-1 0] [1 0] [0 -1] [0 1]]))))
