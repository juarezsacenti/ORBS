package br.ufsc.lapesd.orbs.tokit;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class TrainingData {
    private final AbstractMap<String, User> users;
    private final AbstractMap<String, Item> items;
    private final AbstractMap<String, UserItemEvent> ratingEvents;

	public TrainingData(AbstractMap<String, User> usersRDD, AbstractMap<String, Item> itemsRDD,
			AbstractMap<String, UserItemEvent> ratingEventsRDD) {
		this.users = usersRDD;
		this.items = itemsRDD;
		this.ratingEvents = ratingEventsRDD;
	}

	public AbstractMap<String, User> getUsers() {
        return users;
    }

    public AbstractMap<String, Item> getItems() {
        return items;
    }

    public AbstractMap<String, UserItemEvent> getEvents() {
        return ratingEvents;
    }

    public void sanityCheck() {
        if (users.isEmpty()) {
            throw new AssertionError("User data is empty");
        }
        if (items.isEmpty()) {
            throw new AssertionError("Item data is empty");
        }
        if (ratingEvents.isEmpty()) {
            throw new AssertionError("Rating Event data is empty");
        }
    }
    
    public void toPrint() {
    	for(UserItemEvent event : ratingEvents.values()) {
    		System.out.println(""+event.getUser()+"; "+event.getItem()+"; "+event.getRatingValue());
    	}
    }
}
