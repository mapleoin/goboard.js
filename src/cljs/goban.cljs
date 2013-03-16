(ns goboard.goban
  (:use [goboard.utils :only [indexed]]))

(def ^:const pixel 0.5)  ; hack to draw pixel-perfect lines
(def ^:const dot-radius 2)  ; radius of the 9 board dots

(defn setup-board [board]
  (set! (. (board :canvas) -height) (board :size))
  (set! (. (board :canvas) -width) (board :size)))

(defn draw-background [board]
  (set! (. (board :context) -fillStyle) (board :background))
  (let [x 0
        y 0
        width (+ (board :inner) (* 2 (board :space)))
        height width]
    (. (board :context) (fillRect x y width height))))

(defn draw-lines [board]
  (let [close-edge (board :offset)
        far-edge (+ (board :offset) (board :inner))]
    (doseq [x (take (board :lines)
                    (iterate (partial + (board :space))
                             (+ pixel (board :offset))))]
      ;; horizontal lines
      (. (board :context) (moveTo close-edge x))
      (. (board :context) (lineTo far-edge x))
      ;; vertical lines
      (. (board :context) (moveTo x close-edge))
      (. (board :context) (lineTo x far-edge))))
  (set! (. (board :context) -strokeStyle) (board :markings-color))
  (. (board :context) (stroke)))

(defn draw-letters [board]
  (set! (. (board :context) -textBaseline) "top")
  (set! (. (board :context) -fillStyle) (board :markings))
  (dotimes [i (board :lines)]
    (. (board :context) (fillText (get "abcdefghjklmnopqrst" i)
                                  (+ (* i (board :space)) (board :offset))
                                  (+ (board :inner) (* (/ 5 4) (board :space)))))
    (. (board :context) (fillText (- (board :lines) i)
                                  (+ (board :inner) (* (/ 5 4) (board :space)))
                                  (+ (* i (board :space)) (board :offset))))))

(defn draw-circle
  "Draw a circle on the board at the coordinates indicated by x and y."
  [board x y radius fill-color border-color]
  (. (board :context) (beginPath))
  (. (board :context) (arc (+ (board :offset) pixel (* x (board :space)))
                           (+ (board :offset) pixel (* y (board :space)))
                           radius 0 (* 2 (. js/Math -PI)) false))
  (. (board :context) (closePath))
  (set! (. (board :context) -strokeStyle) border-color)
  (set! (. (board :context) -fillStyle) fill-color)
  (. (board :context) (fill))
  (. (board :context) (stroke)))
  
(defn draw-dots
  "Draw standard goban dots in the nine points a.k.a. hoshi"
  [board]
  (doseq [[x y] [[3  3] [9  3] [15  3]
                 [3  9] [9  9] [15  9]
                 [3 15] [9 15] [15 15]]]
    (draw-circle board x y dot-radius (board :markings) (board :markings))))

(defn draw-stone
  "Draw a stone on the board.
   - color is one of: 1 - black, 2 - white
   - x and y are zero-indexed board coordinates starting from the top left
   corner of the board"
  [board color x y]
  (let [fill-color (if (= color 1) "black" "white")]
    (draw-circle board x y (board :stone-radius) fill-color "black")))

(defn draw-board
  "Draw a board with stones
  - stones is a vector of 19*19 values where each value is either:
    0 - empty, 1 - black stone, 2 - white stone"
  [board stones]
  (setup-board board)
  (draw-background board)
  (draw-lines board)
  (draw-letters board)
  (draw-dots board)
  (doseq [[i color] (indexed stones)]
    (if (contains? #{1 2} color)
      (let [x (mod i (board :lines))
            y (quot i (board :lines))]
        (draw-stone board color x y)))))

(defn draw-last-move [board x y]
  (draw-circle board x y dot-radius "red" "red"))