
package org.opensilk.common.mortar;

import org.opensilk.common.mortar.HasScope;

/**
 * Implemented by {@link android.app.Activity} instances whose pause / resume state
 * is to be shared. The activity must call {@link PauseAndResumePresenter#activityPaused()}
 * and {@link PauseAndResumePresenter#activityResumed()} at the obvious times.
 */
public interface PauseAndResumeActivity extends HasScope {
    boolean isRunning();
}
