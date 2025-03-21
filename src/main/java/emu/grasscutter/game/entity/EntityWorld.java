package emu.grasscutter.game.entity;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.world.*;
import emu.grasscutter.net.proto.SceneEntityInfoOuterClass.SceneEntityInfo;
import it.unimi.dsi.fastutil.ints.*;
import lombok.Getter;

public class EntityWorld extends GameEntity {
    @Getter private World world;

    public EntityWorld(World world) {
        super(null);

        this.world = world;
        this.id = world.getNextEntityId(EntityIdType.MPLEVEL);

        this.initAbilities();
    }

    @Override
    public Scene getScene() {
        return this.world.getHost().getScene();
    }

    @Override
    public void initAbilities() {
        // Load abilities from levelElementAbilities
        var configGlobalCombat = GameData.getConfigGlobalCombat();
         if (configGlobalCombat != null) {
             for (var ability :
                     configGlobalCombat.getDefaultAbilities().getDefaultMPLevelAbilities()) {
                 var data = GameData.getAbilityData(ability);
                 if (data != null) world.getHost().getAbilityManager().addAbilityToEntity(this, data);
             }
         } else {
             // 处理 configGlobalCombat 为 null 的情况，例如记录日志
        }
    }

    @Override
    public int getEntityTypeId() {
        return EntityIdType.TEAM.getId();
    }

    @Override
    public Int2FloatMap getFightProperties() {
        // TODO
        return new Int2FloatArrayMap();
    }

    @Override
    public Position getPosition() {
        // TODO Auto-generated method stub
        return new Position(0, 0, 0);
    }

    @Override
    public Position getRotation() {
        return new Position(0, 0, 0);
    }

    @Override
    public SceneEntityInfo toProto() {
        return null;
    }
}
