package org.alopezme.example.springtester.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Team {

    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.NO)")
    @ProtoField(number = 1, required = true)
    public String teamName;
    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.YES)")
    @ProtoField(number = 2, required = true)
    public String description;
    @ProtoDoc("@Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)")
    @ProtoField(number = 3, collectionImplementation = ArrayList.class)
    public List<String> players;

    public Team() {
    }

    @ProtoFactory
    public Team(String teamName, String description, String[] players) {
        this.teamName = teamName;
        this.description = description;
        this.players = Arrays.asList(players);
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String toJsonString() {
        StringBuilder b = new StringBuilder("{");
        b.append("\"_type\":\"" + "com.example.clientdatagrid.Team" + "\",");
        b.append("\"teamName\":\"").append(teamName).append("\",");
        b.append("\"description\":\"").append(description).append("\",");
        b.append("\"players\":[");
        Iterator<String> iterator = players.iterator();
        while(iterator.hasNext()){
            b.append("\"").append(iterator.next()).append("\"");
            if (iterator.hasNext())
                b.append(",");
            else
                b.append("]");
        }
        b.append("}"); // Close team
        return b.toString();

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Team " + teamName + " (" + description + ") with");
        for (String player : players) {
            b.append(" ").append(player);
        }
        return b.toString();
    }
}
