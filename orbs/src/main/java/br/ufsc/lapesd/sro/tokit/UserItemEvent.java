package br.ufsc.lapesd.sro.tokit;

public class UserItemEvent {
    private final String user;
    private final String item;
    private final long time;
    private final UserItemEventType type;
    private final float ratingValue;

    public UserItemEvent(String user, String item, long time, UserItemEventType type) {
        this.user = user;
        this.item = item;
        this.time = time;
        this.type = type;
        this.ratingValue = -1;
    }
    
    public UserItemEvent(String user, String item, long time, UserItemEventType type, float ratingValue) {
        this.user = user;
        this.item = item;
        this.time = time;
        this.type = type;
        this.ratingValue = ratingValue;
    }

    public String getUser() {
        return user;
    }

    public String getItem() {
        return item;
    }

    public long getTime() {
        return time;
    }

    public UserItemEventType getType() {
        return type;
    }

    public float getRatingValue() {
		return ratingValue;
	}

	@Override
    public String toString() {
        return "UserItemEvent{" +
                "user='" + user + '\'' +
                ", item='" + item + '\'' +
                ", time=" + time +
                ", type=" + type +
                ", rating=" + ratingValue +
                '}';
    }
}
