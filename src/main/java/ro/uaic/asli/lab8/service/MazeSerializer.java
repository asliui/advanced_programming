package ro.uaic.asli.lab8.service;

import ro.uaic.asli.lab8.model.Maze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MazeSerializer {

    public void saveMaze(Maze maze, File file) throws IOException {
        if (maze == null) {
            throw new IllegalArgumentException("Maze is null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(maze);
        }
    }

    public Maze loadMaze(File file) throws IOException, ClassNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            return (Maze) obj;
        }
    }
}

