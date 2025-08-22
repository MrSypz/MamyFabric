package com.sypztep.mamy.common.system.party;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PartyData {
    private final UUID partyId;
    private String partyName;
    private UUID leaderId;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final Map<UUID, Long> inviteTimestamps = new HashMap<>();
    private long createdTimestamp;
    private int maxMembers = 6; // Default party size

    // Party settings
    private boolean isPublic = false;
    private boolean allowInvites = true;
    private String description = "";

    public PartyData(UUID partyId, String partyName, UUID leaderId) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.leaderId = leaderId;
        this.createdTimestamp = System.currentTimeMillis();
        this.members.add(leaderId); // Leader is always a member
    }

    // ===== GETTERS =====
    public UUID getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public UUID getLeaderId() { return leaderId; }
    public Set<UUID> getMembers() { return new HashSet<>(members); }
    public Set<UUID> getInvitedPlayers() { return new HashSet<>(invitedPlayers); }
    public int getMemberCount() { return members.size(); }
    public int getMaxMembers() { return maxMembers; }
    public boolean isPublic() { return isPublic; }
    public boolean allowsInvites() { return allowInvites; }
    public String getDescription() { return description; }

    // ===== PARTY MANAGEMENT =====
    public boolean canJoin() {
        return members.size() < maxMembers;
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return playerId.equals(leaderId);
    }

    public boolean isInvited(UUID playerId) {
        return invitedPlayers.contains(playerId);
    }

    public boolean addMember(UUID playerId) {
        if (!canJoin()) return false;

        // Remove from invited list and add to members
        invitedPlayers.remove(playerId);
        inviteTimestamps.remove(playerId);
        return members.add(playerId);
    }

    public boolean removeMember(UUID playerId) {
        if (isLeader(playerId) && members.size() > 1) {
            // Transfer leadership before removing
            transferLeadership();
        }
        return members.remove(playerId);
    }

    public boolean invitePlayer(UUID playerId) {
        if (!allowInvites || !canJoin() || isMember(playerId)) return false;

        invitedPlayers.add(playerId);
        inviteTimestamps.put(playerId, System.currentTimeMillis());
        return true;
    }

    public void removeInvite(UUID playerId) {
        invitedPlayers.remove(playerId);
        inviteTimestamps.remove(playerId);
    }

    public void transferLeadership() {
        // Transfer to first available member (not leader)
        for (UUID memberId : members) {
            if (!memberId.equals(leaderId)) {
                leaderId = memberId;
                return;
            }
        }
    }

    public void transferLeadership(UUID newLeaderId) {
        if (isMember(newLeaderId)) {
            leaderId = newLeaderId;
        }
    }

    // ===== SETTINGS =====
    public void setPartyName(String name) { this.partyName = name; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setAllowInvites(boolean allowInvites) { this.allowInvites = allowInvites; }
    public void setDescription(String description) { this.description = description; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = Math.max(2, Math.min(12, maxMembers)); }

    // ===== INVITE CLEANUP =====
    public void cleanupExpiredInvites() {
        long currentTime = System.currentTimeMillis();
        Set<UUID> expiredInvites = new HashSet<>();

        for (Map.Entry<UUID, Long> entry : inviteTimestamps.entrySet()) {
            if (currentTime - entry.getValue() > 300000) { // 5 minutes
                expiredInvites.add(entry.getKey());
            }
        }

        for (UUID playerId : expiredInvites) {
            removeInvite(playerId);
        }
    }

    // ===== NBT SERIALIZATION =====
    public void writeToNbt(NbtCompound nbt) {
        nbt.putUuid("PartyId", partyId);
        nbt.putString("PartyName", partyName);
        nbt.putUuid("LeaderId", leaderId);
        nbt.putLong("CreatedTimestamp", createdTimestamp);
        nbt.putInt("MaxMembers", maxMembers);
        nbt.putBoolean("IsPublic", isPublic);
        nbt.putBoolean("AllowInvites", allowInvites);
        nbt.putString("Description", description);

        // Serialize members
        NbtList membersList = new NbtList();
        for (UUID memberId : members) {
            membersList.add(NbtString.of(memberId.toString()));
        }
        nbt.put("Members", membersList);

        // Serialize invited players
        NbtList invitedList = new NbtList();
        for (UUID invitedId : invitedPlayers) {
            invitedList.add(NbtString.of(invitedId.toString()));
        }
        nbt.put("InvitedPlayers", invitedList);

        // Serialize invite timestamps
        NbtCompound timestampsNbt = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : inviteTimestamps.entrySet()) {
            timestampsNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("InviteTimestamps", timestampsNbt);
    }

    public static PartyData readFromNbt(NbtCompound nbt) {
        UUID partyId = nbt.getUuid("PartyId");
        String partyName = nbt.getString("PartyName");
        UUID leaderId = nbt.getUuid("LeaderId");

        PartyData party = new PartyData(partyId, partyName, leaderId);
        party.createdTimestamp = nbt.getLong("CreatedTimestamp");
        party.maxMembers = nbt.getInt("MaxMembers");
        party.isPublic = nbt.getBoolean("IsPublic");
        party.allowInvites = nbt.getBoolean("AllowInvites");
        party.description = nbt.getString("Description");

        // Read members
        party.members.clear();
        NbtList membersList = nbt.getList("Members", 8);
        for (int i = 0; i < membersList.size(); i++) {
            try {
                UUID memberId = UUID.fromString(membersList.getString(i));
                party.members.add(memberId);
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
            }
        }

        // Read invited players
        NbtList invitedList = nbt.getList("InvitedPlayers", 8);
        for (int i = 0; i < invitedList.size(); i++) {
            try {
                UUID invitedId = UUID.fromString(invitedList.getString(i));
                party.invitedPlayers.add(invitedId);
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
            }
        }

        // Read invite timestamps
        NbtCompound timestampsNbt = nbt.getCompound("InviteTimestamps");
        for (String key : timestampsNbt.getKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                long timestamp = timestampsNbt.getLong(key);
                party.inviteTimestamps.put(playerId, timestamp);
            } catch (IllegalArgumentException e) {
                // Skip invalid entries
            }
        }

        return party;
    }

    // ===== UTILITY =====
    public Text getFormattedName() {
        return Text.literal("[" + partyName + "]").formatted(Formatting.GOLD);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PartyData partyData = (PartyData) obj;
        return Objects.equals(partyId, partyData.partyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partyId);
    }
}