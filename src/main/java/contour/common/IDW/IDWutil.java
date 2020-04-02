package contour.common.IDW;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wContour.Contour;
import wContour.Interpolate;
import wContour.Global.Border;
import wContour.Global.PolyLine;
import wContour.Global.Polygon;

/**
 * IDWutil
 * 
 * @author xuwei
 */
public class IDWutil {

    private Logger logger = LoggerFactory.getLogger(IDWutil.class);

    private static final int DEFAULT_ALGORITHM_ROWS = 200;
	private static final int DEFAULT_ALGORITHM_COLS = 200;
    private static final double DEFAULT_ALGORITHM_UNDEFINE = -9999.0;

    //data[0]--> longitude array, data[1]-->latitude array, data[2]-->kpi data array
    private double[][] data;

    private double[] colorValues;

    private double left, right, top, bottom;

    public IDWutil(double[][] data, double[] colorValues, double left, double right, double top, double bottom){
        this.data = data;
        this.colorValues = colorValues;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public List<Polygon> interpolate(){
        logger.info("IDW算法开始插值...");
        double[] x = new double[DEFAULT_ALGORITHM_ROWS];
		double[] y = new double[DEFAULT_ALGORITHM_COLS];
        int neighborNumber = colorValues.length - 1;
        
        // 填充数据
		Interpolate.CreateGridXY_Num(left, bottom, right, top, x, y);
		double[][] gridData = Interpolate.Interpolation_IDW_Neighbor(
				data, x, y, neighborNumber, DEFAULT_ALGORITHM_UNDEFINE);

		int nc = colorValues.length;
		int[][] S1 = new int[gridData.length][gridData[0].length];

		// 训练等值线
		List<Border> borders = Contour.tracingBorders(gridData, x, y, S1, DEFAULT_ALGORITHM_UNDEFINE);
		List<PolyLine> contourLines = Contour.tracingContourLines(gridData, x, y, nc,
				colorValues, DEFAULT_ALGORITHM_UNDEFINE, borders, S1);

		// 平滑处理
		contourLines = Contour.smoothLines(contourLines);

		// 训练等值面
		List<Polygon> contourPolygons = Contour.tracingPolygons(gridData, contourLines,
				borders, colorValues);
		Collections.sort(contourPolygons, new Comparator<Polygon>() {
			@Override
			public int compare(Polygon o1, Polygon o2) {
				return Double.compare(o1.LowValue, o2.LowValue);
			}
        });
        

        return contourPolygons;
    }



    
}