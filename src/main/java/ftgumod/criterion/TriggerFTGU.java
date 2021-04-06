package ftgumod.criterion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public abstract class TriggerFTGU<T extends TriggerFTGU.Instance> implements ICriterionTrigger<T> {

	protected final Map<PlayerAdvancements, Set<Listener<T>>> listeners = new HashMap<>();
	private final ResourceLocation id;

	public TriggerFTGU(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public void addListener(PlayerAdvancements playerAdvancements, Listener<T> listener) {
		listeners.computeIfAbsent(playerAdvancements, p -> new HashSet<>()).add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements playerAdvancements, Listener<T> listener) {
		Set<Listener<T>> listeners = this.listeners.get(playerAdvancements);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				this.listeners.remove(playerAdvancements);
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements playerAdvancements) {
		listeners.remove(playerAdvancements);
	}

	public abstract class Instance extends AbstractCriterionInstance {

		public Instance() {
			super(id);
		}

	}

}
