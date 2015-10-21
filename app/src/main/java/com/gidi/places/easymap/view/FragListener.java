package com.gidi.places.easymap.view;

import com.gidi.places.easymap.model.Place;

/**
 * Public Interface that activity should implement
 * to be the listener for the fragments
 */
public interface FragListener {
    void onPlaceSelected(Place place);

}
