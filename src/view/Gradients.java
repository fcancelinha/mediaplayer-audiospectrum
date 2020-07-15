package view;

public enum Gradients {

    SUMMER  ("-fx-background-color: linear-gradient(to left, #FF3366, #BA265D);"),
    MOSS    ("-fx-background-color: linear-gradient(to left, #FFE000, #799F0C);"),
    LEMON   ("-fx-background-color: linear-gradient(to right, #e65c00, #f9d423);"),
    MIST    ("-fx-background-color: linear-gradient(to right, #649173, #dbd5a4);"),
    BLOOD   ("-fx-background-color: linear-gradient(to right, #a80000, #ff0202);"),
    ZEST    ("-fx-background-color: linear-gradient(to right, #ffa100, #ffcf22);"),
    WAVES   ("-fx-background-color: linear-gradient(to right, #1fc4d3, #1396a3);"),
    SUNRISE ("-fx-background-color: linear-gradient(to right, #00f1ff, #f4b300);"),
    DARKMODE("-fx-background-color: linear-gradient(to right, #28313B, #485461);"),
    LIGHMODE("-fx-background-color: White;"),
    SELECT  ("-fx-background-color:  rgba(0, 0, 0, 0.4);"),
    TRANSPARENT ("-fx-background-color:  Transparent");

    private String style;

    Gradients(String style){
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

}
