package ftgumod.technology;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Criterion;

public class TechnologyProgress {

	private final Map<String, Boolean> criteria = Maps.newHashMap();
	private String[][] requirements = new String[0][];

	public void update(Map<String, Criterion> p_update_1_, String[][] p_update_2_) {
		Set<String> lvt_3_1_ = p_update_1_.keySet();
		Iterator lvt_4_1_ = this.criteria.entrySet().iterator();

		while (lvt_4_1_.hasNext()) {
			Map.Entry<String, Boolean> lvt_5_1_ = (Map.Entry) lvt_4_1_.next();
			if (!lvt_3_1_.contains(lvt_5_1_.getKey())) {
				lvt_4_1_.remove();
			}
		}

		lvt_4_1_ = lvt_3_1_.iterator();

		while (lvt_4_1_.hasNext()) {
			String lvt_5_2_ = (String) lvt_4_1_.next();
			if (!this.criteria.containsKey(lvt_5_2_)) {
				this.criteria.put(lvt_5_2_, false);
			}
		}

		this.requirements = p_update_2_;
	}

	public Iterable<String> getRemaningCriteria() {
		List<String> lvt_1_1_ = Lists.newArrayList();
		Iterator var2 = this.criteria.entrySet().iterator();

		while (var2.hasNext()) {
			Map.Entry<String, Boolean> lvt_3_1_ = (Map.Entry) var2.next();
			if (!(lvt_3_1_.getValue())) {
				lvt_1_1_.add(lvt_3_1_.getKey());
			}
		}

		return lvt_1_1_;
	}

	public boolean grantCriterion(String p_grantCriterion_1_) {
		Boolean lvt_2_1_ = this.criteria.get(p_grantCriterion_1_);
		if (lvt_2_1_ != null && !lvt_2_1_) {
			this.criteria.put(p_grantCriterion_1_, true);
			return true;
		} else {
			return false;
		}
	}

	public Iterable<String> getCompletedCriteria() {
		List<String> lvt_1_1_ = Lists.newArrayList();
		Iterator var2 = this.criteria.entrySet().iterator();

		while (var2.hasNext()) {
			Map.Entry<String, Boolean> lvt_3_1_ = (Map.Entry) var2.next();
			if (lvt_3_1_.getValue()) {
				lvt_1_1_.add(lvt_3_1_.getKey());
			}
		}

		return lvt_1_1_;
	}

	public boolean revokeCriterion(String p_revokeCriterion_1_) {
		Boolean lvt_2_1_ = this.criteria.get(p_revokeCriterion_1_);
		if (lvt_2_1_ != null && lvt_2_1_) {
			this.criteria.put(p_revokeCriterion_1_, false);
			return true;
		} else {
			return false;
		}
	}

	public boolean isDone() {
		if (this.requirements.length == 0) {
			return false;
		} else {
			String[][] var1 = this.requirements;
			int var2 = var1.length;

			for (int var3 = 0; var3 < var2; ++var3) {
				String[] lvt_4_1_ = var1[var3];
				boolean lvt_5_1_ = false;
				String[] var6 = lvt_4_1_;
				int var7 = lvt_4_1_.length;

				for (int var8 = 0; var8 < var7; ++var8) {
					String lvt_9_1_ = var6[var8];
					Boolean lvt_10_1_ = this.getCriterionProgress(lvt_9_1_);
					if (lvt_10_1_ != null && lvt_10_1_) {
						lvt_5_1_ = true;
						break;
					}
				}

				if (!lvt_5_1_) {
					return false;
				}
			}

			return true;
		}
	}

	@Nullable
	public Boolean getCriterionProgress(String p_getCriterionProgress_1_) {
		return this.criteria.get(p_getCriterionProgress_1_);
	}

	public boolean hasProgress() {
		Iterator var1 = this.criteria.values().iterator();

		Boolean lvt_2_1_;
		do {
			if (!var1.hasNext()) {
				return false;
			}

			lvt_2_1_ = (Boolean) var1.next();
		} while (!lvt_2_1_);

		return true;
	}

}
