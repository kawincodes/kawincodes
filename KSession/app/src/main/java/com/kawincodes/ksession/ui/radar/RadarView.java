package com.kawincodes.ksession.ui.radar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Animated neon radar: rings, crosshair, rotating sweep, phosphor-fading blips, labels and pulses.
 * XML attribute {@code app:radarCompact="true"} draws a small avatar version (no labels/ticks).
 */
public class RadarView extends View {

    public interface OnBlipClickListener {
        void onBlipClick(RadarBlip blip);
    }

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // TODO shell
    }

    /** Replaces the blips (animate new ones in). */
    public void setBlips(List<RadarBlip> blips) {
        // TODO shell
    }

    public void setCompact(boolean compact) {
        // TODO shell
    }

    /** Pause/resume the sweep animation (e.g. when nothing is running, slow idle sweep). */
    public void setSweepEnabled(boolean enabled) {
        // TODO shell
    }

    /** Center text, e.g. "3 ONLINE"; null for none (ignored in compact mode). */
    public void setCenterLabel(@Nullable String label) {
        // TODO shell
    }

    public void setOnBlipClickListener(@Nullable OnBlipClickListener l) {
        // TODO shell
    }
}
