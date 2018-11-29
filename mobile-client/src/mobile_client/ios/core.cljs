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
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))

(def Amplify (js/require "aws-amplify"))
(def Auth (gobj/get Amplify "Auth"))
(def Analytics (gobj/get Amplify "Analytics"))
(def aws-exports (js/require "./aws-exports.js"))
(def awsconfig (gobj/get aws-exports "default"))
(.configure Auth awsconfig)
(.configure Analytics awsconfig)

(def logo-img (js/require "./images/cljs.png"))

#_(let [desired-user (clj->js {:username "test-user1"
                             :password "hardToGuess1!"
                             :attributes {:email "chris@hlprmnky.com"
                                          :phone_number "+16513530691"}})]
  (-> (.signUp Auth desired-user)
      (.then #(.log js/console %))
      (.catch #(.log js/console %))))
(def current-user (atom {:user nil}))

(comment
  (-> (.confirmSignUp Auth "test-user1" "023929")
      (.then #((swap! current-user assoc :user %)
               (js/console.log %)))
      (.catch #(js/console.log %)))

  (-> (.currentSession Auth)
      (.then #(js/console.log %))
      (.catch #(js/console.log "Error in currentSession:" %)))

  (-> (.currentAuthenticatedUser Auth)
      (.then #(js/console.log %))
      (.catch #(js/console.log "Error in currentAuthenticatedUser:" %)))

  )

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        username (subscribe [:get-username])
        password (subscribe [:get-password])
        user     (subscribe [:get-user])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 18 :font-weight "100" :margin-bottom 20 :text-align "center"}}
        (str "User: " (if-not @user "None!" (.-username @user)))]
       [view {:style {:flex-direction "row" :margin 10 :align-items "center"}}
        [text {:style {:font-size 18 :font-weight "100" :margin-bottom 0 :text-align "right"}} "Username:"]
        [text-input {:default-value     "user name"
                     :id                "username"
                     :auto-focus        true
                     :on-change-text    #(dispatch [:set-username %])
                     :style             {:flex      1
                                         :fontSize  18
                                         :textAlign "center"
                                         :margin   4
                                         :background-color "#bbb"}}]]
       [view {:style {:flex-direction "row" :margin 10 :align-items "center"}}
        [text {:style {:font-size 18 :font-weight "100" :margin-bottom 0 :text-align "right"}} "Password:"]
        [text-input {:default-value     "password"
                     :id                "password"
                     :auto-focus        true
                     :on-change-text    #(dispatch [:set-password %])
                     :style             {:flex      1
                                         :fontSize  18
                                         :textAlign "center"
                                         :margin   4
                                         :background-color "#bbb"}}]]
       [view {:style {:flex-direction "row" :margin 10 :align-items "center"}}
        [touchable-highlight {:style {:background-color "#999" :margin 10 :padding 10 :border-radius 5}
                              :on-press #(-> (.signIn Auth @username @password)
                                             (.then (fn [user]
                                                      (.record Analytics (clj->js {:name "userSignedIn"
                                                                                  :attributes
                                                                                  {:username (.-username user)}}))
                                                      (dispatch [:set-authenticated-user user])))
                                             (.catch (fn [err]
                                                       (js/console.log "Error in signIn:" err))))}
         [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Log In"]]
        [touchable-highlight {:style {:background-color "#999" :margin 10 :padding 10 :border-radius 5}
                              :on-press #(-> (.signOut Auth)
                                             (.then (js/console.log "User was signed out.")
                                                    (.record Analytics (clj->js {:name "userSignedOut"}))
                                                    (dispatch [:set-authenticated-user nil]))
                                             (.catch (fn [e] (js/console.log "Error in signOut:" e))))}
         [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Log Out"]]]
       [view {:style {:flex-direction "row" :margin 10 :align-items "center"}}]])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "mobileClient" #(r/reactify-component app-root)))
