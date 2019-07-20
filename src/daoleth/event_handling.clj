(ns daoleth.event-handling
  (:use seesaw.core)
  (:require [daoleth.core :as dc]))

(defn get-state-from-file [filename]
  ; TODO Write a fun for loading from file
  )
(defn write-state-to-file [state]
  ;TODO Write a fn for writing to file
  )

(defn make-dialog [& items]
  (dialog
   :type :question
   :success-fn #(map text (select % [:<javax.swing.JTextField>]))
   :option-type :ok-cancel
   :content (vertical-panel :items items)))

(defn event-handler [e state]
  (case (config e :id)
    :event-new-level
    (reset! state dc/initial-state)
    (let [results (show! (make-dialog "Level Name:"
                                      (text :text "unkown-level" :id :level-name)))]
      (swap! state assoc :filename (first results)))

    :event-open-level
    (let [results (show! (make-dialog "Level Name:"
                                      (text :text "unkown-level" :id :level-name)))]
      (reset! state (get-state-from-file (first results))))

    :event-save-level
    (do
      (swap! state assoc :saved? true)
      (write-state-to-file @state))

    :event-create-object
    (let [results (show! (make-dialog "Object Name:"
                                      (text :text "crate" :id :object-name)
                                      "Object Character:"
                                      (text :text "=" :id :object-char)))
          object [(keyword (first results)) (first (second results))]]
      (swap! state assoc :objects (conj (:objects @state) object)))))
