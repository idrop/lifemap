(ns lifemap.server
  (:use
   ring.adapter.jetty 
   compojure.core
   ring.util.response
   [compojure.response :as resp]
   [lifemap.data :as data]
   [lifemap.utils :as utils]
   [compojure.route :as route]))


(defn- respond
  "given a list of events, sanitizes each event, jsonizes it, then renders reponse"
  [coll request]
  (->>
   (utils/clean-response coll)
   (utils/to-json)
   (resp/render request)))


;; an event looks like this when it comes in as params
;; { "lng" "-0.13677564177418056" "lat" "51.311495428201916" "fbid" "642791293"
;;    "when" "62985600000" "desc" "description" "tag" "some tag"}

(defroutes main-routes


  (POST "/add" {params :params request :request}
        (let [event ((comp translate keywordize) params)]
          (if (utils/valid? event)
            (->
             (dissoc event :id)
             (data/add ,,,)
             (respond ,,, request)))))

  
  (POST "/update" {params :params request :request}
        (let [event ((comp translate keywordize) params)]
          (if (utils/valid? event)
            (respond (data/update event) request))))

  
  (POST "/list" [fbid {request :request}] 
        (->
         (utils/string-to-long fbid)
         (data/all ,,,)
         (respond ,,, request)))

  
  (POST "/delete" [id {request :request}]
        (respond (data/delete id) request))

  
  ;; given GET http://localhost:8888/ping?foo=test, returns "pong test"
  (GET "/ping" [foo]
       (str "pong " foo))

  
  ;; all other content is served out of public/  
  (route/files "/" {:root "public"})


  ;; why doesn't this work?
  (route/not-found "Not found."))


(defn start []
  (run-jetty #'main-routes {:port 8888 :join? false}))


