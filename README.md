SiGame Local

A local multiplayer quiz game inspired by TV quiz shows. The game allows multiple players to connect from their phones while the host controls the game from a computer and a separate screen displays the game for everyone.

Features
Support for up to 10 players
Local network multiplayer
Host-controlled game flow
Real-time communication using WebSockets
Question packs loaded from JSON
Text and image-based questions
Player scoring system
Timed answering
Separate host, player, and TV interfaces
Visual presentations and game animations
Technologies
Java
Spring Boot
WebSocket
REST API
JavaScript
HTML / CSS
JSON
Git / GitHub
How it works
The host starts the Spring Boot application.
Players connect to the game using their phones through the local network.
The host loads a question pack and starts the game.
Questions are displayed on the TV screen.
Players can press the answer button when they know the answer.
The host manages the answers and game flow.
Scores are updated throughout the game.
Project Structure
host.html — host interface for controlling the game
player.html — player interface
tv.html — TV/display interface
src/main/java — backend application
src/main/resources — frontend resources and game data
Purpose

This project was created as a personal software development project to practice backend development, real-time communication, REST APIs, frontend development, and working with structured game data
