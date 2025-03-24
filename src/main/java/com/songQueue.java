package org.sample.sampleGUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sample.sampleGUI.APIhandler.Song;

public class songQueue {
    final LinkedList<Song> queue = new LinkedList<>();
    private int currentIndex = -1;

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void add(Song song) {
        queue.add(song);
        if (currentIndex == -1) currentIndex = 0;
    }

    public synchronized List<Song> getQueue() {
        return new ArrayList<>(queue);
    }

    public void setNowPlaying(Song song) {
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    public synchronized int getCurrentIndex() {
        return currentIndex;
    }

    public synchronized Song getCurrent() {
        return (currentIndex >= 0 && currentIndex < queue.size()) ? queue.get(currentIndex) : null;
    }

    public synchronized void clear() {
        queue.clear();
        currentIndex = -1;
    }

    public synchronized Song next() {
        if (currentIndex + 1 < queue.size()) {
            return queue.get(++currentIndex);
        }
        currentIndex = -1; // Reset when reaching the end
        return null;
    }

    public synchronized void remove(int index) {
        if (index < 0 || index >= queue.size()) return;

        queue.remove(index);

        if (index < currentIndex) {
            currentIndex--;
        } else if (index == currentIndex) {
            currentIndex = -1;
        }
    }

    public synchronized void moveToTop(int index) {
        if (index <= 0 || index >= queue.size()) return;

        Song song = queue.remove(index);
        queue.add(1, song);

        if (index <= currentIndex) {
            currentIndex++;
        }
    }

    public void removePlayed() {
        if (currentIndex >= 0) {
            queue.remove(currentIndex);
            currentIndex = Math.max(-1, currentIndex - 1);
        }
    }
}

