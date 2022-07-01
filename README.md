# Capsa Banting

The capsa game is usually played by 4 players and must use all 52 cards but the joker card is discarded. At the beginning of the game, these 52 cards are dealt until they run out. So each player gets 13 cards. 

Why is it called Capsa Banting? Because when we take out the card, we slam it. In the capsa banting game, whoever has issued all his cards first wins.

# Techs
Java programming language, NetBeans IDE, Web Services, Multithreading, and MySQL database

# Features
1.	User login. Login credentials are stored in the database, where only the server can accessed the database directly
2.	Sign Up for new user
3. Room List (after successful login)
4. Rooms can be created only through the server
5. All players can select and join a room
6. In the room, there are only 4 players. If 4 people have entered, the 5th person who tries to enter cannot enter the room
7. To start the game, all players must press the 'START' button. When the four players have pressed the button, the four players will get 13 cards randomly (from 52 variations of playing cards)
8. The first round starts with the player who has 3 diamonds. To put the card on the board, a "PUT" button is provided. The method of placing follows the Capsa rules: single card, double, threess, packet, and so on
9. The PUT button can only appear if it is the player's turn to place without time limit to place the card
10. The order of the next players is determined by the server at the start of the game randomly
11. The order of the next players follows the rules of placing on the first player. For example: a player who has 3 diamonds plays a single card, then the next player plays a single card. The game lasts until all players press the pass button
12. The next round starts with which player has the highest card in the previous round. Steps as in number 8
13. The game ends when there is one player whose cards have run out

# Screenshots
Room List  
![Room List](https://github.com/vari-ang/Capsa-Banting/blob/master/screenshots/Rooms.png)

Waiting Room  
![Waiting Room](https://github.com/vari-ang/Capsa-Banting/blob/master/screenshots/Waiting%20Room.png)

Game
![Game](https://github.com/vari-ang/Capsa-Banting/blob/master/screenshots/Game%20-%201.png)

![Game](https://github.com/vari-ang/Capsa-Banting/blob/master/screenshots/Game%20-%202.png)
