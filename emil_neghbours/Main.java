package com.company;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.awt.*;

import static java.lang.System.*;


public class Main extends Application {

    //setup for standard var
    public final static int worldSize = 300;
    public static Plot[][] world = new Plot[worldSize][worldSize]; // world var

    public static int dotSize = 2; // display vars

    public static float satisfaction = 0.7f; // needed satisfaction
    public static int emptyProcent = 50; // the percentage of plots which is set as empty
    public static int amountTypes = 2;

    long previousTime = nanoTime(); //timer var
    final long interval = 450000000;



    public static void main(String[] args) {
        Random rng = new Random();
        for(int i = 0; i < worldSize; i++)  //initializing world
        {
            for (int j = 0; j < worldSize; j++)
            {
                int r = rng.nextInt(100);
                if (r <= emptyProcent) world[i][j] = new Plot(new Position(i, j), 0);
                else {
                    int t = rng.nextInt(amountTypes) +1;
                    world[i][j] = new Plot(new Position(i, j), t);
                }
            }
        }
	launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception{
        primaryStage.setTitle("neighbours");

        Group root = new Group();
        Canvas canvas = new Canvas(worldSize * (dotSize), worldSize * (dotSize));
        GraphicsContext gc =  canvas.getGraphicsContext2D();
        root.getChildren().addAll(canvas);

        AnimationTimer timer =  new AnimationTimer() { //timer
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if(elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        timer.start();
    }

    public void renderWorld (GraphicsContext g) // render world
    {
        g.clearRect(0,0, worldSize * (dotSize), worldSize*(dotSize));
        for(int i = 0; i < worldSize; i++)
        {
            for (int j = 0; j < worldSize; j++)
            {
                int x = dotSize*j;
                int y = dotSize*i;

                g.setFill(world[i][j].color);
                g.fillOval(x,y,dotSize,dotSize);
            }
        }
    }

    public void updateWorld () // updates world;
    {
        int unsatisfied = 0;
        for(int i = 0; i < worldSize; i++)
        {
            for (int j = 0; j < worldSize; j++)
            {
                if (world[i][j].type != 0)
                {
                        if (!world[i][j].isSatisfied(satisfaction, world)) { //if the plot is unsatisfied we find a random empty plot and change position of the plots
                        Plot emptyPlot = FindRandomEmpty();

                        world[i][j].SwitchPosition(emptyPlot);
                        unsatisfied ++;
                    }
                }
            }
        }

        System.out.println("number of unsatisfied citizens: " + unsatisfied);
        if (unsatisfied == 0) {
            System.out.println("Everyone is satisfied!");
            System.exit(1);
        }
    }

    public Plot FindRandomEmpty () // this method can be replaced by for example a method that searches for the closest empty plot.
    {
        Random rng = new Random ();
        ArrayList<Plot> emptyPlots = new ArrayList<Plot>();
        for(int i = 0; i < worldSize; i++)
        {
            for (int j = 0; j < worldSize; j++)
            {
                if(world[i][j].type == 0)
                {
                    emptyPlots.add(world[i][j]);
                }
            }
        }
        return emptyPlots.get(rng.nextInt(emptyPlots.size()));
    }
}

class Plot
{
    public Position position; // position of plot in world;
    public int type;    // the type of plot, 0 is empty plot, everything else is different types

    public Color color;

    public Plot (Position newPosition, int newType){
        position = newPosition;
        type = newType;
        ColorCorrect();
    }

    public void ColorCorrect ()
    {
        Color[] colors = {Color.WHITE, Color.BLUE,Color.RED,Color.GREEN, Color.YELLOW, Color.MAGENTA,Color.GRAY,Color.BLACK,Color.BROWN,Color.HONEYDEW,Color.OLIVEDRAB,Color.ORANGE};
        color=colors[type];
    }

    public boolean isSatisfied (float satisfaction, Plot[][] world) // check if plot is satisfied;
    {
        float avg = 0;
        int n = 0;
        for (int i = position.x - 1; i < position.x+2; i++)
        {
            for (int j = position.y -1; j < position.y+2; j++)
            {
                if (i > -1 && j > -1 //check that i or j isn't outside of world or at object pos
                        && i < world.length && j < world.length
                        && (i != this.position.x || j != this.position.y)) {
                    // System.out.println("at ("+position.x + ", "+ position.y +") "+ " x: " + i + " y: " + j);

                    if (world[i][j].type == this.type)
                    {
                        avg ++;
                    }
                    if (world[i][j].type != 0) //they don't care about empty plots
                    {
                        n++;
                    }
                }
            }
        }
        if (n == 0) //In some cases one plot is encircled with empty plots witch causes the n value to be set at 0. This if-statement prevents divide by zero
            return true;
        else {
            avg /= n;
            //System.out.println(avg);
            return avg >= satisfaction;
        }
    }

    public void SwitchPosition (Plot otherPlot) //Easy way to switch plots
    {
        int t = type;
        type=otherPlot.type;
        otherPlot.type = t;

        ColorCorrect();
        otherPlot.ColorCorrect();
    }
}

class Position // Position class
{
    int x;
    int y;

    Position (int newX, int newY)
    {
        x = newX;
        y = newY;
    }
}
