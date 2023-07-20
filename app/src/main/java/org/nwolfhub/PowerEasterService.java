package org.nwolfhub;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

@Deprecated
public class PowerEasterService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    public void execute() {
        performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
    }
}
