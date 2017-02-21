/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appliedmed.exercises;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author zmichaels
 */
public class Prediction {

    private static class Point {

        final double x;
        final double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Interpolates between the 2D points stored in the list
     *
     * @param points list of points
     * @return interpolated list of points
     */
    private static List<Point> interpolate(List<Point> points) {
        /*
         * TODO: implement this.
         * 40% of the List's points were deleted (set to null). Interpolate
         * those points. You may either do this in-place or calculate a new List.
         */

        int nullStart = -1;
        int nullEnd = -1;
        int nullCount = 0;
        int zeroNullEnd = -1;
        int lastNullStart = -1;
        for(int i = 0; i < points.size(); i++) {
            if(points.get(i) == null) {
                int j = i;

                nullCount++;
                while(j+1 < points.size()) {
                    if(points.get(j+1) == null) {
                        j++;
                        nullCount++;
                    } else {
                        break;
                    }
                }
                if(j == points.size()) {j--;}
                if(i==0){
                    zeroNullEnd = j;
                }
                if(j == points.size() -1) {
                    lastNullStart = i;
                }
                fillNulls(i,j,points);
                i = j+1;
                continue;
            }
        }
        fillNullsFromZero(zeroNullEnd, points);
        fillNullsFromLast(lastNullStart,points);
        System.out.println(nullCount);
        return points;
    }

    private static void fillNullsFromLast(int lastNullStart, List<Point> points) {
        if(lastNullStart == -1) return;
        double ydelta = points.get(lastNullStart - 1).y - points.get(lastNullStart - 2).y;
        double xdelta = points.get(lastNullStart - 1).x - points.get(lastNullStart - 2).x;
        double xstart = points.get(lastNullStart - 1).x;
        double ystart = points.get(lastNullStart - 1).y;
        int loc = 1;
        for(int k = lastNullStart; k <= points.size() - 1; k++) {
            points.set(k, new Point(xstart + loc * xdelta, ystart + loc * ydelta));
            loc++;
        }
    }

    private static void fillNullsFromZero(int zeroNullEnd,List<Point> points ) {
        if(zeroNullEnd == -1) return;
        double ydelta = points.get(zeroNullEnd + 2).y - points.get(zeroNullEnd + 1).y;
        double xdelta = points.get(zeroNullEnd + 2).x - points.get(zeroNullEnd + 1).x;
        double xstart = points.get(zeroNullEnd + 1).x - xdelta * zeroNullEnd + 1;
        double ystart = points.get(zeroNullEnd + 1).y - ydelta * zeroNullEnd + 1;
        int loc = 1;
        for(int k = 0; k <= zeroNullEnd; k++) {
            points.set(k, new Point(xstart + loc * xdelta, ystart + loc * ydelta));
            loc++;
        }
    }

    private static void fillNulls(int i, int j, List<Point> points) {
        System.out.println("filling : " + i + " " + j);
        if(i == 0 || j == points.size() -1) {
            return;
        }

        double ydelta = (points.get(j+1).y - points.get(i-1).y) / (j-i + 2);
        double xdelta = (points.get(j+1).x - points.get(i-1).x) / (j-i + 2);
        double xstart = points.get(i-1).x;
        double ystart = points.get(i-1).y;
        int loc = 1;
        for(int k = i; k <= j; k++) {
            points.set(k, new Point(xstart + loc * xdelta, ystart + loc * ydelta));
            loc++;
        }

    }

    public static void main(String[] args) throws Exception {
        final Path pExpected = Paths.get("expected.csv");
        final List<Point> expected;

        // load the expected values
        try (BufferedReader in = Files.newBufferedReader(pExpected)) {
            expected = in.lines()
                    .skip(1)
                    .map(str -> str.split(","))
                    .map(p -> new Point(Double.parseDouble(p[0]), Double.parseDouble(p[1])))
                    .collect(Collectors.toList());
        }

        // delete 40% of the values
        final List<Point> actual = new ArrayList<>(expected);

        for (int i = 0; i < actual.size(); i++) {
            if (Math.random() < 0.4) {
                actual.set(i, null);
            }
        }

        // create the interpolated list
        final List<Point> interpolated = interpolate(actual);

        final JFrame window = new JFrame("Interpolation");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(640, 480);
        window.setVisible(true);
        window.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                final int width = this.getWidth();
                final int height = this.getHeight();

                g.clearRect(0, 0, width, height);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, width, height);

                final BufferedImage surface = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

                // draw the expected values as cyan
                for (Point p : expected) {
                    final int x = (int) p.x;
                    final int y = (int) p.y;

                    surface.setRGB(x, y, 0xFF00FFFF);
                }

                // draw the interpolated values as yellow
                for (Point p : interpolated) {
                    if (p != null) {
                        final int x = (int) p.x;
                        final int y = (int) p.y;

                        surface.setRGB(x, y, 0xFFFFFF00);
                    }
                }

                g.drawImage(surface, 0, 0, width, height, 0, 0, 256, 256, null);
            }
        });
    }
}
