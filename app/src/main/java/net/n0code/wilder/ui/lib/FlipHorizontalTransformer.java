package net.n0code.wilder.ui.lib;

import android.view.View;

public class FlipHorizontalTransformer extends BaseTransformer {

    @Override
    protected void onTransform(View view, float position) {
        final float rotation = 180f * position;

        view.setAlpha(rotation > 90f || rotation < -90f ? 0 : 1);
        view.setPivotX(view.getWidth() * 0.5f);
        view.setPivotY(view.getHeight() * 0.5f);
        view.setRotationY(rotation);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isPagingEnabled() {
        return true;
    }

}