(ns medegas.handlers
  (:require
   [meinside.clogram :as cg]
   [medegas.api :as api]
   
   [clojure.string :as cstr]))

(defn- get-file
  [bot file-id]
  (let [{:keys [file-url file-path file-id file-unique-id]} (:result (cg/get-file bot file-id))]
    {:url file-url
     :output (str file-unique-id ".oga")}))

#_(use 'clojure.pprint)

(defn- when-not-result [result]
  (when-not (:ok result)
    (println "*** failed to send message:" (:reason-phrase result))))

(def msg-about (str "Olá! Eu sou um bot do Telegram que pode ajudá-lo a saber a "
                    "porcentagem de gás em seu botijão apenas ouvindo o som de uma "
                    "batida de colher nele! Tudo que você precisa fazer é enviar um "
                    "áudio com o som da batida de colher no botijão para mim e eu vou "
                    "usar a minha tecnologia de reconhecimento de som para interpretar "
                    "a frequência da batida e calcular a porcentagem de gás restante no "
                    "botijão. Em seguida, eu enviarei uma mensagem de volta para "
                    "você com a porcentagem de gás atualizada. É rápido e fácil! "
                    "Experimente agora e tenha sempre a certeza de quanto gás ainda tem em casa."))

(def msg-help
  (str "Se você deseja calibrar o bot para obter uma precisão ainda maior na medição do gás em seu botijão, siga estas etapas: \n\n"

       "1. Certifique-se de que seu botijão esteja vazio e que você possa gravar um som nítido da batida de uma colher no botijão.\n"
       "2. Envie o áudio que você gravou do som da batida no botijão vazio e digite o comando /vazio.\n"
       "3. Certifique-se de que seu botijão esteja completamente cheio e que você possa gravar um som nítido da batida de colher no botijão cheio.\n"
       "4. Envie o áudio que você gravou do som da batida de colher no botijão cheio e digite o comando /cheio.\n\n"
 
       "Isso permitirá que o bot se ajuste para reconhecer os sons do seu botijão específico e fornecer medições ainda mais precisas da "
       "porcentagem de gás restante. Se tiver alguma dúvida, não hesite em entrar em contato conosco. Obrigado por usar o nosso bot!"))

(def sound (atom {}))

(defn about
  [bot chat-id]
  (let [result (cg/send-message bot chat-id msg-about)]
    (when-not-result result)))

(defn help
  [bot chat-id]
  (let [result (cg/send-message bot chat-id msg-help)]
    (when-not-result result)))

(defn start [bot chat-id]
  (let [result (cg/send-message bot chat-id (str "Olá, bem vindo(a)! \n" msg-help))]
    (when-not-result result)))

(defn- select-sound [chats chat-id]
  (println chat-id)
  (-> (filterv (fn [[k v]]
                (= k (str chat-id))) @chats)
      (first)))

(defn- insert-sound [chats chat-id sound-id]
  (swap! chats assoc (str chat-id) sound-id))

(defn- make-resquest [bot file msg-id chat-id] 
  (let [file-payload (get-file bot file)
        user-payload {:id (str chat-id) :sound-id msg-id}
        payload (merge user-payload file-payload)]
    (api/pitch-detect payload)))

(defn audios [bot chat-id msg-id file]
  (let [{:keys [result id]} (make-resquest bot file msg-id chat-id)
        _ (println result id)
        mede-text (cond (string? result) result
                        (and (<= 0 result) (>= 100 result)) (str "seu gás esta aproximadamente em: " result "% \n" 
                                                                    "Gostaria de usar como calibragem? \n"
                                                                    "basta /cheio se estiver cheio e "
                                                                    "/vazio se estiver vazio")
                        :else "ERROR: envie um audio novamente") 
        result (cg/send-message bot chat-id mede-text
                                :reply-to-message-id msg-id)]
    (insert-sound sound chat-id id)
    (when-not-result result)))

(defn types [bot chat-id types]
  (let [[_ sound] (select-sound sound chat-id)
        _ (api/sound-type {:id sound :types types :user chat-id})
        result (cg/send-message bot chat-id "salvo!")] 
    (when-not-result result)))

(defn handle-bot
  [bot update]
  (let [chat-id (get-in update [:message :chat :id])
        msg-id (get-in update [:message :message-id])
        text (get-in update [:message :text])
        file (get-in update [:message :voice :file-id])]
    (let [result (cg/send-chat-action bot chat-id :typing)]
      (when-not-result result))
    (cond 
      file (audios bot chat-id msg-id file) 
      (= text "/about") (about bot chat-id)
      (= text "/help") (help bot chat-id)
      (= text "/start") (start bot chat-id)
      (= text "/cheio") (types bot chat-id "full")
      (= text "/vazio") (types bot chat-id "empty")
      :else "mande novamente")))
