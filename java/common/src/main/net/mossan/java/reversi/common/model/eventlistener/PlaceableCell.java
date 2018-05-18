package net.mossan.java.reversi.common.model.eventlistener;

import java.util.List;

public final class PlaceableCell {
    public final int[] placePoint;
    public final List<int[]> reversiblePoints;

    public PlaceableCell(int[] placePoint, List<int[]> reversiblePoints) {
        this.placePoint = placePoint;
        this.reversiblePoints = reversiblePoints;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlaceableCell))
            return false;
        final PlaceableCell other = (PlaceableCell) obj;

        if (this.placePoint[0] != other.placePoint[0] || this.placePoint[1] != other.placePoint[1])
            return false;

        if (this.reversiblePoints.size() != other.reversiblePoints.size())
            return false;
        for (int i = 0; i < this.reversiblePoints.size(); ++i) {
            final int[] thisRP = this.reversiblePoints.get(i);
            final int[] otherRP = other.reversiblePoints.get(i);
            if (thisRP[0] != otherRP[0] || thisRP[1] != otherRP[1])
                return false;
        }

        return true;
    }
}
