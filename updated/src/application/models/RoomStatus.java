// src/main/java/application/models/RoomStatus.java
package application.models;

public class RoomStatus {
    private String roomNo;
    private String status;

    public RoomStatus() {}

    public RoomStatus(String roomNo, String status) {
        this.roomNo = roomNo;
        this.status = status;
    }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RoomStatus{" +
                "roomNo='" + roomNo + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
