(ns daoleth.core
  (:gen-class)
  (:require [daoleth.constants :as dc]
            [daoleth.event-handling :as evt]
            [seesaw.bind :as bind])
  (:use seesaw.core clojure.data))

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
    (horizontal-panel :items (conj buttons (label :id :filename
                                                  :text (str (:filename @state) "  "))))))

(defn get-obj-pair [key map]
  (let [x (key map)] [x (x (:objects map))]))

(defn beautify [[name chr]]
  (let [name (clojure.string/join (rest (str name)))
        chr (str "(" chr ")")]
    (str
     (clojure.string/capitalize name)
     "  "
     chr)))

(defn generate-squares [set-state state]
  (let [outline        #(config! %2 :background (if %1 :grey :white))
        button-handler (fn [loc e]
                         (outline true e)
                         (when (:drawing? @state)
                           (let [val ((:painting-object @state) (:objects @state))
                                 new-map (assoc (:map @state) loc val)]
                             (text! e (str val))
                             (set-state :map new-map))))]
    (map #(label :background :white
                 :h-text-position :center
                 :v-text-position :center
                 :text (str %1)
                 :listen [:mouse-entered   (partial button-handler %2)
                          :mouse-exited    (partial outline false)
                          :mouse-pressed   (fn [e]
                                             (println "Pressed")
                                             (set-state :drawing? true)
                                             (button-handler %2 e))
                          :mouse-released  (fn [_]
                                             (println "Released")
                                             (set-state :drawing? false))])
         (:map @state)
         (iterate inc 1))))

(defn make-split-view [state]
  (let [set-state   (fn [a b]
                      (swap! state assoc a b))
        object-list (listbox :id :objects
                             :model (:objects @state)
                             :renderer (fn [this {:keys [value]}]
                                         (text! this (beautify value)))
                             :listen [:selection
                                      (fn [e]
                                        (when (selection e)
                                          (set-state :painting-object (first (selection e)))))])
        squares     (generate-squares set-state state)
        grid-map    (grid-panel :rows (first (:size @state))
                                :columns (second (:size @state))
                                :items squares
                                :id :canvas
                                :hgap 0 :vgap 0)]
    (selection! object-list (get-obj-pair :painting-object @state))
    (left-right-split (scrollable object-list) (scrollable grid-map) :divider-location 1/3)))

(defn add-behaviors [root state]
  (add-watch state :filechange (fn [k r o n]
                                 (println "Updating filename")
                                 (config! (select root [:#filename]) :text (:filename n))
                                 (when (or (not (= (:objects o) (:objects n)))
                                           (or (:new-file? o) (:new-file? n)))
                                   (println "Updating objects")
                                   (config! (select root [:#objects]) :model (:objects n))
                                   (swap! state assoc :new-file? false)
                                   (let [set-state #(swap! state assoc %1 %2)
                                         squares (generate-squares set-state r)]
                                     (config! (select root [:#canvas]) :items squares)))))
  root)

(defn -main [& args]
  (native!)
  (let [state (atom dc/initial-state)]
    (if (not (empty? args))
      (reset! state (dc/get-state-from-file (first args)))
      (dc/create-initial-map state))
    (invoke-later
     (-> (frame :title "Daoleth Level Editor"
                :content (border-panel
                          :north (make-menu-bar state)
                          :center (make-split-view state)
                          :vgap 5 :hgap 5 :border 5))
         (add-behaviors state)
         pack!
         show!))))
