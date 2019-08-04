(ns daoleth.constants
  (:use [clojure.set]))

(def initial-state {:objects {:wall \# :door \o :floor \.}
                    :filename "unknown"
                    :map nil
                    :size [20 20]
                    :saved? false
                    :mode :paintbrush
                    :painting-object :floor})

(defn make-2d-map [[width height] base]
  (vec (replicate height (vec (replicate width base)))))

(defn create-initial-map [state]
  (swap! state assoc :map (make-2d-map (:size @state)
                                       (get-in @state [:objects :floor]))))

(defn get-state-from-file [filename]
  (->> filename slurp read-string))

(defn write-state-to-file [state]
  (spit (:filename state) (with-out-str
                            (-> state
                                (select-keys [:map :size :filename :objects])
                                pr))))

(defn +p [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn draw-map [m]
  (for [line m]
    (println (clojure.string/join " "
                                  (clojure.core/map str line)))))

(defn map-effect [type m [pos-x pos-y] val]
  (case type
    :paintbrush
    (update-in m [pos-y pos-x] (fn [_] val))
    :bucketfill
    (let [start-val (get-in m [pos-y pos-x])]
      (loop [points (set [[pos-x pos-y]])
             nm m]
        (let [inside? (fn [[x y]]
                        (and (>= x 0) (>= y 0)
                             (< x (count (first m)))
                             (< y (count m))))
              point (first points)
              old-val (get-in nm (reverse point))]
          (cond
            (and (= old-val start-val) (not (= old-val val)))
            (let [nm1 (update-in nm (reverse point) (fn [_] val))
                  mods [[0 1] [1 0] [-1 0] [0 -1]]
                  new-points (union (rest points)
                                    (set (filter inside?
                                                 (map +p (repeat point) mods))))]
              (recur new-points nm1))
            (> (count points) 0) (recur (rest points) nm)
            :else nm))))))
