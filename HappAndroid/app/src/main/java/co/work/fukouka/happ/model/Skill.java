package co.work.fukouka.happ.model;


import java.util.List;

public class Skill {
    String category;
    int skillId;
    String name;
    List<Skill> skills;

    public Skill() {
    }

    public Skill(String category, List<Skill> skills) {
        this.category = category;
        this.skills = skills;
    }

    public Skill(int skillId, String name) {
        this.skillId = skillId;
        this.name = name;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
}
