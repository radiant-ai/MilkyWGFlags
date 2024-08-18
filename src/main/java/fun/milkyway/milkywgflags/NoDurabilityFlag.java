package fun.milkyway.milkywgflags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class NoDurabilityFlag extends Flag<Material[]> {
    public NoDurabilityFlag(String name) {
        super(name);
    }

    @Override
    public Material[] parseInput(FlagContext context) throws InvalidFlagFormat {
        var userInput = context.getUserInput();
        String[] inputMaterials = userInput.split(",");
        Material[] materials = new Material[inputMaterials.length];

        for (int i = 0; i < inputMaterials.length; i++) {
            String materialName = inputMaterials[i].trim().toUpperCase();
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                throw new InvalidFlagFormat("Invalid material: " + inputMaterials[i].trim());
            }
            materials[i] = material;
        }

        return materials;
    }

    @Override
    public Material[] unmarshal(@Nullable Object o) {
        if (o instanceof String) {
            String[] materialNames = ((String) o).split(",");
            Material[] materials = new Material[materialNames.length];
            for (int i = 0; i < materialNames.length; i++) {
                materials[i] = Material.getMaterial(materialNames[i].trim().toUpperCase());
                if (materials[i] == null) {
                    return null;
                }
            }
            return materials;
        } else if (o instanceof Material[]) {
            return (Material[]) o;
        }
        return null;
    }

    @Override
    public Object marshal(Material[] o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i].name());
            if (i < o.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
