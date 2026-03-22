package mia.modmod.render.screens;

import net.minecraft.util.Mth;

import java.util.function.Function;

public class Animation {
    private AnimationStage animationStage;
    private float progress;
    private final Function<Float, Float> easingFunction;

    public Animation(AnimationStage animationStage, float progress, Function<Float, Float> easingFunction) {
        this.animationStage = animationStage;
        this.progress = progress;
        this.easingFunction = easingFunction;
    }

    public void setAnimationStage(AnimationStage animationStage) {
        this.animationStage = animationStage;
    }
    public void setAnimation(float animation) {
        this.progress = animation;
    }

    public AnimationStage getAnimationStage() {
        return this.animationStage;
    }
    public float getProgress() {
        return easingFunction.apply(this.progress);
    }

    public void updateAnimation(float delta) {
        this.progress = Mth.clamp(progress + (delta * animationStage.direction), 0f, 1f);
        if (progress == 1 && animationStage.equals(AnimationStage.OPENING)) animationStage = AnimationStage.OPEN;
        if (progress == 0 && animationStage.equals(AnimationStage.CLOSING)) animationStage = AnimationStage.CLOSED;
    }
}
