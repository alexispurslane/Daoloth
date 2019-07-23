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
        buttons  (map #(button :text (first %)
                               :id (second %)
                               :listen [:action (fn [e]
                                                  (evt/event-handler e state))])
                      commands)
        names     [["Paint" :paintbrush]
                   ["Bucket" :bucketfill]]
        tools     (combobox :model names :renderer (fn [this {:keys [value]}]
                                                     (text! this (first value)))
                            :listen [:action (fn [e]
                                               (swap! state assoc :mode (second (selection e))))])

        items (vec (concat buttons
                           [tools (label :id :filename
                                         :text (str "    " (:filename @state)))]))]
    (println items)
    (horizontal-panel :items items)))

(defn get-obj-pair [key map]
  (let [x (key map)] [x (x (:objects map))]))

(defn beautify [[name chr]]
  (let [name (clojure.string/join (rest (str name)))
        chr (str "(" chr ")")]
    (str
     (clojure.string/capitalize name)
     "  "
     chr)))

(defn generate-squares [state]
  (let [outline #(config! %2 :background (if %1 :grey :white))]
    (map #(label :background :white
                 :h-text-position :center
                 :v-text-position :center
                 :text (str %1)
                 :listen [:mouse-entered   (fn [e]
                                             (outline true e)
                                             (evt/event-handler e :draw state %2))
                          :mouse-exited    (partial outline false)
                          :mouse-pressed   (fn [e]
                                             (evt/event-handler e :start-draw state %2)
                                             (evt/event-handler e :draw state %2))
                          :mouse-released  (fn [e]
                                             (evt/event-handler e :end-draw state %2))])
         (apply concat (:map @state))
         (iterate inc 0))))

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
        squares     (generate-squares state)
        grid-map    (grid-panel :rows (first (:size @state))
                                :columns (second (:size @state))
                                :items squares
                                :id :canvas
                                :hgap 0 :vgap 0)]
    (selection! object-list (get-obj-pair :painting-object @state))
    (left-right-split (scrollable object-list) (scrollable grid-map) :divider-location 1/3)))

(defn add-behaviors [root state]
  (add-watch state :statechange (fn [k r o n]
                                  (println "Updating filename")
                                  (config! (select root [:#filename])
                                           :text (str "    " (:filename n) (if (:saved? n) "" "+")))
                                  (when (or (not (= (:objects o) (:objects n)))
                                            (or (:new-file? o) (:new-file? n)))
                                    (println "Updating objects")
                                    (config! (select root [:#objects]) :model (:objects n))
                                    (swap! state assoc :new-file? false)
                                    (config! (select root [:#canvas])
                                             :items (generate-squares r)))))
  root)

(defn -main [& args]
  (native!)
  (let [state (atom dc/initial-state)]
    (if (not (empty? args))
      (reset! state (dc/get-state-from-file (first args)))
      (dc/create-initial-map state))
    (if (nil? (:painting-object @state))
      (swap! state assoc :painting-object (last (keys (:objects @state)))))
    (invoke-later
     (-> (frame :title "Daoleth Level Editor"
                :content (border-panel
                          :north (make-menu-bar state)
                          :center (make-split-view state)
                          :vgap 5 :hgap 5 :border 5))
         (add-behaviors state)
         pack!
         show!))))
