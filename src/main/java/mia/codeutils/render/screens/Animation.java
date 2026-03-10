package mia.codeutils.render.screens;

import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;

public class Animation {
    private AnimationStage animationStage;
    private float animation;
    private final Function<Float, Float> easingFunction;

    public Animation(AnimationStage animationStage, float animation, Function<Float, Float> easingFunction) {
        this.animationStage = animationStage;
        this.animation = animation;
        this.easingFunction = easingFunction;
    }

    public void setAnimationStage(AnimationStage animationStage) {
        this.animationStage = animationStage;
    }
    public void setAnimation(float animation) {
        this.animation = animation;
    }

    public AnimationStage getAnimationStage() {
        return this.animationStage;
    }
    public float getAnimation() {
        return easingFunction.apply(this.animation);
    }

    public void updateAnimation(float delta) {
        this.animation = Mth.clamp(animation + (delta * animationStage.direction), 0f, 1f);
        if (animation == 1 && animationStage.equals(AnimationStage.OPENING)) animationStage = AnimationStage.OPEN;
        if (animation == 0 && animationStage.equals(AnimationStage.CLOSING)) animationStage = AnimationStage.CLOSED;
    }
}
