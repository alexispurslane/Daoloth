(ns daoleth.event-handling
  (:use seesaw.core)
  (:require [daoleth.constants :as dc]))

(defn make-dialog [& items]
  (dialog
   :type :question
   :success-fn (fn [root] root)
   :option-type :ok-cancel
   :content (vertical-panel :items items)))

(defn event-handler
  ([e state]
   (case (config e :id)
     :event-new-level
     (let [model    (spinner-model 20 :from 2 :to 100)
           root     (show! (make-dialog "Level Name:"
                                        (text :text "unkown-level" :id :level-name)
                                        (horizontal-panel
                                         :items ["Size: "
                                                 (spinner :id :width :model model)
                                                 (spinner :id :height :model model)])))]
       (when root
         (let [filename (text (select root [:#level-name]))
               width    (value (select root [:#width]))
               height   (value (select root [:#height]))]
           (reset! state dc/initial-state)
           (swap! state assoc :filename filename)
           (swap! state assoc :size [width height])
           (dc/create-initial-map state)
           (swap! state assoc :new-file? true)
           (println @state))))

     :event-open-level
     (let [root (show! (make-dialog "Level Name:"
                                    (text :text "unkown-level" :id :level-name)))]
       (when root
         (reset! state (dc/get-state-from-file (text (select root [:#level-name]))))
         (swap! state assoc :new-file? true)
         (println @state)))

     :event-save-level
     (do
       (swap! state assoc :saved? true)
       (dc/write-state-to-file @state))

     :event-create-object
     (let [root    (show! (make-dialog "Object Name:"
                                       (text :text "crate" :id :object-name)
                                       "Object Character:"
                                       (text :text "=" :id :object-char)))]
       (when root
         (let [results (map text (select root [:<javax.swing.JTextField>]))
               object  [(keyword (first results)) (first (second results))]]
           (swap! state assoc :objects (conj (:objects @state) object)))))))
  ([e event-type state loc]
   (case event-type
     :draw
     (when (:drawing? @state)
       (let [val            ((:painting-object @state) (:objects @state))
             [width height] (:size @state)
             [pos-x pos-y]  [(mod loc width)
                             (mod (int (/ loc width)) height)]
             new-map        (update-in (:map @state) [pos-y pos-x] (fn [o] val))]
         (text! e (str val))
         (swap! state assoc :map new-map)))

     :start-draw
     (swap! state assoc :drawing? true)

     :end-draw
     (swap! state assoc :drawing? false))))
