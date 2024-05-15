I've developed a classic chess game using Java, and I utilized a variety of resources that Java offers. The main components of my game include a graphical user interface (GUI), the game logic, and a game loop that manages how the game progresses.

For the GUI, I used JFrame as the main window that contains everything. Inside it, I have a GamePanel, which extends JPanel. This panel is responsible for drawing the chessboard and the pieces and handling user inputs like mouse clicks to move the pieces. Essentially, when you open the game, JFrame pops up and displays the GamePanel.

For the game logic, I've created classes for each type of chess piece, like King, Queen, Rook, and so on. These classes either extend a base class or implement an interface that defines common behaviours. For instance, the King class has a method to check if a move is valid specifically for the King. All these pieces are placed on a chessboard, which I represent as a 2D array. The board handles the initialization and management of these pieces.

I've also implemented the rules and regulations of chess. This includes checking for valid moves, check, and checkmate conditions. For example, there's a method that determines if the current player is in checkmate.

To manage the flow of the game, I implemented a simple game loop using the Runnable interface. This game loop updates the game state and renders the game at regular intervals, aiming for a smooth experience, roughly at 60 frames per second. The loop continuously runs, updating and rendering, until the game is stopped.

Additionally, I've included methods to handle user interactions. When you click on the board, the program calculates which tile you clicked on and processes the move. Utility classes help with various tasks, like converting coordinates and validating moves.

Overall, my implementation ensures that the proper rules of chess are followed, providing a functional and enjoyable experience for players.
