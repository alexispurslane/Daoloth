(ns daoleth.core
  (:gen-class)
  (:require [daoleth.event-handling :as evt]
            [seesaw.bind :as bind])
  (:use seesaw.core))

(def initial-state {
                    :objects {:wall \# :door \o :floor \.}
                    :filename "unknown"
                    :map nil
                    :size [20 20]
                    :saved? false
                    :painting-object :floor
                    })

(defn make-menu-bar [state]
  (let [commands [["New"  :event-new-level]
                  ["Open" :event-open-level]
                  ["Save" :event-save-level]
                  ["+"    :event-create-object]]
        buttons (map #(button :text (first %)
                              :id (second %)
                              :listen [:action (fn [e]
                                                 (evt/event-handler e state))])
                     commands)]
    (horizontal-panel :items buttons)))

(defn get-obj-pair [key map]
  (let [x (key map)] [x (x (:objects map))]))

(defn beautify [[name chr]]
  (let [name (clojure.string/join (rest (str name)))
        chr (str "(" chr ")")]
    (str
     (clojure.string/capitalize name)
     "  "
     chr)))

(defn make-split-view [state]
  (let [set-state #(swap! state assoc %1 %2)
        outline   #(config! %2 :background (if %1 :grey :white))
        button-handler (fn [loc e]
                         (outline true e)
                         (when (:drawing? @state)
                           (println "Moved")
                           (let [val ((:painting-object @state) (:objects @state))
                                 new-map (assoc (:map @state) loc val)]
                             (text! e (str val))
                             (swap! state assoc :map new-map))))
        object-list (listbox :model (:objects @state)
                             :renderer (fn [this {:keys [value]}]
                                         (text! this (beautify value)))
                             :listen [:selection
                                      #(set-state :painting-object (first (selection %)))])
        squares (map #(label :background :white
                             :h-text-position :center
                             :v-text-position :center
                             :text (str %1)
                             :listen [:mouse-entered   (partial button-handler %2)
                                      :mouse-exited    (partial outline false)
                                      :mouse-pressed   (fn [_]
                                                         (set-state :drawing? true))
                                      :mouse-released  (fn [_]
                                                         (set-state :drawing? false))])
                     (:map @state)
                     (iterate inc 1))
        grid-map (grid-panel :rows (first (:size @state))
                             :columns (second (:size @state))
                             :items squares
                             :hgap 0 :vgap 0)]
    (selection! object-list (get-obj-pair :painting-object @state))
    (left-right-split (scrollable object-list) (scrollable grid-map) :divider-location 1/3)))

(defn -main [& args]
  (native!)
  (let [state (atom initial-state)]
    (swap! state assoc :map (vec (replicate (apply * (:size @state)) \.)))
    (println (:map @state))
    (invoke-later
     (-> (frame :title "Daoleth Level Editor"
                :content (border-panel
                          :north (make-menu-bar state)
                          :center (make-split-view state)
                          :vgap 5 :hgap 5 :border 5))
         pack!
         show!))))
