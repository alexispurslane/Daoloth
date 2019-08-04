(ns daoleth.core
  (:gen-class)
  (:require [daoleth.constants :as dc]
            [daoleth.event-handling :as evt]
            [seesaw.bind :as bind]
            [seesaw.mouse :as mouse])
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
        items (vec (concat buttons
                           [(label :id :filename
                                   :text (str "    " (:filename @state)))]))]
    (horizontal-panel :items items)))

(defn- get-obj-pair [key map]
  (let [x (key map)] [x (x (:objects map))]))

(defn- beautify [[name chr]]
  (let [name (clojure.string/join (rest (str name)))
        chr (str "(" chr ")")]
    (str
     (clojure.string/capitalize name)
     "  "
     chr)))

(defn- generate-squares [state]
  (let [outline #(config! %2 :background (if %1 :grey :white))]
    (map #(label :background :white
                 :h-text-position :center
                 :v-text-position :center
                 :text (str %1)
                 :id (keyword (str "el-" %2))
                 :listen [:mouse-entered   (fn [e]
                                             (outline true e)
                                             (when (mouse/button-down? e :left)
                                               (evt/event-handler e :draw state %2)))
                          :mouse-pressed   (fn [e] (evt/event-handler e :draw state %2))
                          :mouse-exited    (partial outline false)])
         (apply concat (:map @state))
         (iterate inc 0))))

(defn make-split-view [state]
  (let [set-state   (fn [a b]
                      (swap! state assoc a b))
        names     [["Paint" :paintbrush]
                   ["Bucket" :bucketfill]]
        tools       (combobox :model names
                              :renderer (fn [this {:keys [value]}]
                                          (text! this (first value)))
                              :listen [:action
                                       (fn [e]
                                         (swap! state assoc
                                                :mode (second (selection e))))])
        object-list (listbox :id :objects
                             :model (:objects @state)
                             :renderer (fn [this {:keys [value]}]
                                         (text! this (beautify value)))
                             :listen [:selection
                                      (fn [e]
                                        (when (selection e)
                                          (set-state
                                           :painting-object (first (selection e)))))])
        sidebar     (top-bottom-split tools
                                      (scrollable object-list)
                                      :divider-location 1/5)
        squares     (generate-squares state)
        grid-map    (grid-panel :rows (first (:size @state))
                                :columns (second (:size @state))
                                :items squares
                                :id :canvas
                                :hgap 0 :vgap 0)]
    (selection! object-list (get-obj-pair :painting-object @state))
    (left-right-split sidebar (scrollable grid-map) :divider-location 1/3)))

(defn- update-to-match-by-diff [old new canvas]
  (doseq [[old-text new-text index] (map vector old new (iterate inc 0))]
    (when (not (= old-text new-text))
      (println old-text new-text (keyword (str "#el-" index)))
      (text! (select canvas [(keyword (str "#el-" index))]) (str new-text)))))

(defn add-behaviors [root state]
  (add-watch state :statechange (fn [k r o n]
                                  (config! (select root [:#filename])
                                           :text (str "    " (:filename n) (if (:saved? n) "" "+")))
                                  (when (not (= (:map o) (:map n)))
                                    (update-to-match-by-diff (apply concat (:map o))
                                                             (apply concat (:map n))
                                                             (select root [:#canvas]))
                                    (swap! state assoc :new-file? false))
                                  (when (not (= (:objects o) (:objects n)))
                                    (config! (select root [:#objects]) :model (:objects n))
                                    (swap! state assoc :new-file? false))))
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
 
