(ns overtunes.songs.canone-alla-quarta
  (:use
    [overtone.live :only [at now]]
    [overtone.inst.sampled-piano :only [sampled-piano] :rename {sampled-piano piano}]))

(defn sum-n [series n] (reduce + (take n series)))
(defn scale [intervals]
  #(if (neg? %)
     (let [downward-scale (comp - (scale (reverse intervals)))]
       (-> % - downward-scale))
     (sum-n (cycle intervals) %)))

(defn translate [f x y] #(-> % (+ x) f (+ y)))
(def major (scale [2 2 1 2 2 2 1]))
(def g-major (translate major 0 74))

(defn bpm [per-minute] #(-> % (/ per-minute) (* 60) (* 1000)))
(defn syncopate [timing durations] #(->> % (sum-n durations) timing))
(defn run [a b] 
  (if (<= a b)
    (range a (inc b))
    (reverse (run b a))))

(def durations (concat
                 (repeat 2 1/4) [1/2]
                 (repeat 14 1/4) [3/2]
                 (repeat 10 1/4) [1/2]
                 (repeat 2 1/4) [9/4 3/4]
                 (repeat 12 1/4) [1/2 1 1/2]
                 (repeat 12 1/4) [1])) 

(defn update-all [m [& ks] f]
  (if ks
    (update-in
      (update-all m (rest ks) f)
      [(first ks)]
      f)
    m)) 
(defn sharps [notes] #(update-all (vec %) notes inc)) 
(defn flats [notes] #(update-all (vec %) notes dec)) 

; (flats [37])
(def leader-accidentals (comp (sharps [12 22]) (flats [37])))
(def pitches (concat
               [0] (run -1 3) (run 2 0)
               [4] (run 1 8) (run 7 -1)
               [0] (run 0 -3) [4 4] (run 2 -3)
               [-1 1] (run 4 6)
               [1 1 1 2 -1 -2 0 1 -1 -2] (run 5 0)))

(def lower #(- % 7))
;(def bass-accidentals (sharps [8]))
(def bass-accidentals identity)
(def bass (map lower
            (flatten
              (map #(repeat 3 %) (concat (run 0 -3) (run -5 -3) [-7])))))

(defn melody# [timing notes] 
  (let [note# #(at (timing %1) (piano %2))]
    (dorun (map-indexed note# notes)))) 

(defn play# []
  (let [timing (-> (bpm 90) (translate 0 (now)))
        rhythm-from #(syncopate (translate timing % 0) durations)
        leader pitches
        follower (->> leader (map -) (map #(- % 4)))]
    (melody# timing (bass-accidentals (map g-major bass)))
    (melody# (rhythm-from 1/2) (leader-accidentals (map g-major leader)))
    (melody# (rhythm-from 7/2) (map g-major follower))
    (melody# (syncopate (translate timing 23 0) [1 1/4 1/4 1/4 1/4 1]) (map g-major [-7 -7 -5 -3 -1 0]))
    ))

(play#)
