# Monte Java

A multithreaded Java implementation of [MCTS](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search) for the [Game of Amazons](https://en.wikipedia.org/wiki/Game_of_the_Amazons). This AI was created for a class tournament in the course COSC 322 Introduction to Artificial Intelligence.

## Techniques used

- Lockless multithreading
- Efficient chamber analysis using bitwise operations
- Hybrid roullouts with endgame detection
- Splitting players turns into two layers to mitigate the large branching factor

## Usage

To compete with this ai compile the jar with COSC322Test as the main class.

```
# run with gui
java -jar team-01.jar username password
```

```
# run with no gui
java -jar team-01.jar username password nogui
```

 To test locally checkout the LocalTest class!

## Project Structure

All of the functional bits for our AI are in [java.ubc.cosc322.engine](src/main/java/ubc/cosc322/engine). The rest is mostly just code wrapping the prof's API.
