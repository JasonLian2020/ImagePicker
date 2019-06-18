package me.jason.imagepicker;

public interface IntentHub {
    String EXTRA_RESULT_SELECTED_ITEM = "extra_result_selected_item";
    String EXTRA_RESULT_SELECTED_URI = "extra_result_selected_uri";
    String EXTRA_RESULT_SELECTED_PATH = "extra_result_selected_path";
    String EXTRA_RESULT_FROM = "extra_result_from";

    int FROM_NONE = 0;
    int FROM_IMAGE = 1;
    int FROM_VDIEO = 2;

}