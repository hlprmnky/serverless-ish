(ns mobile-client.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [goog.object :as gobj]           
            [mobile-client.events]
            [mobile-client.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def Amplify (js/require "aws-amplify"))
(def Auth (gobj/get Amplify "Auth"))
(def aws-exports (js/require "./aws-exports.js"))
(def awsconfig (gobj/get aws-exports "default"))
(.configure Auth awsconfig)
()
(def logo-img (js/require "./images/cljs.png"))

#_(let [desired-user (clj->js {:username "test-user1"
                             :password "hardToGuess1!"
                             :attributes {:email "chris@hlprmnky.com"
                                          :phone_number "+16513530691"}})]
  (-> (.signUp Auth desired-user)
      (.then #(.log js/console %))
      (.catch #(.log js/console %))))
(def current-user (atom {:user nil}))

(-> (.confirmSignUp Auth "test-user1" "023929")
    (.then #((swap! current-user assoc :user %)
             (js/console.log %)))
    (.catch #(js/console.log %)))

(-> (.signIn Auth "test-user1" "hardToGuess1!")
    (.then #((swap! current-user assoc :user %)
             (js/console.log %)))
    (.catch #(js/console.log "Error in signIn:" %)))

(-> (.currentSession Auth)
    (.then #(js/console.log %))
    (.catch #(js/console.log "Error in currentSession:" %)))

(-> (.currentAuthenticatedUser Auth)
    (.then #(js/console.log %))
    (.catch #(js/console.log "Error in currentAuthenticatedUser:" %)))

(-> (.signOut Auth)
    (.then #(js/console.log "User was signed out."))
    (.catch #(js/console.log "Error in signOut:" %)))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [image {:source logo-img
               :style  {:width 80 :height 80 :margin-bottom 30}}]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(alert "HELLO!")}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "mobileClient" #(r/reactify-component app-root)))
