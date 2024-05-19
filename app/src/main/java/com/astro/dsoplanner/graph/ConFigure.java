package com.astro.dsoplanner.graph;

import android.util.Log;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.MyList;
import com.astro.dsoplanner.base.ConNamePoint;
import com.astro.dsoplanner.base.ConPoint;

import java.util.ArrayList;

//constellations figures
public class ConFigure {
    private static final String TAG = ConFigure.class.getSimpleName();

    public static class data {
        public int hrstart; //the number of initial star in Yale catalog
        public int hrend;//the number of end star in Yale
        public int con;//constellation

        public data(int hs, int he, int con) {
            this.hrstart = hs;
            this.hrend = he;
            this.con = con;
        }
    }

    public static data[] list = {new data(603, 337, 0),
            new data(337, 165, 0),
            new data(165, 15, 0),
            new data(15, 154, 0),
            new data(154, 269, 0),
            new data(269, 464, 0),

            new data(3765, 3871, 0),
            new data(3871, 4104, 0),
            new data(4104, 4273, 0),

            new data(6163, 6102, 0),
            new data(6102, 6020, 0),
            new data(6020, 5470, 0),

            new data(7193, 7236, 0),
            new data(7236, 7377, 0),
            new data(7377, 7525, 0),
            new data(7525, 7557, 0),
            new data(7557, 7602, 0),

            new data(7236, 7235, 0),
            new data(7235, 7377, 0),
            new data(7377, 7570, 0),
            new data(7570, 7710, 0),
            new data(7710, 7447, 0),
            new data(7447, 7236, 0),

            new data(7950, 7990, 0),
            new data(7990, 8232, 0),
            new data(8232, 8414, 0),
            new data(8414, 8518, 0),
            new data(8518, 8559, 0),
            new data(8559, 8539, 0),
            new data(8539, 8414, 0),
            new data(8414, 8499, 0),
            new data(8499, 8698, 0),
            new data(8698, 8834, 0),
            new data(8834, 8858, 0),
            new data(8858, 8709, 0),
            new data(8709, 8679, 0),
            new data(8679, 8698, 0),

            new data(8597, 8559, 0),

            new data(8812, 8858, 0),
            new data(8858, 8892, 0),

            new data(8418, 8232, 0),

            new data(6500, 6462, 0),
            new data(6462, 6285, 0),
            new data(6285, 6295, 0),
            new data(6295, 6510, 0),
            new data(6510, 6461, 0),
            new data(6461, 6462, 0),

            new data(6229, 6285, 0),

            new data(6743, 6510, 0),

            new data(838, 617, 0),
            new data(617, 553, 0),
            new data(553, 546, 0),

            new data(1612, 1605, 0),
            new data(1605, 1708, 0),
            new data(1708, 2088, 0),
            new data(2088, 2077, 0),
            new data(2077, 1708, 0),
            new data(1708, 1641, 0),
            new data(1641, 1577, 0),
            new data(1577, 1791, 0),
            new data(1791, 2095, 0),
            new data(2095, 2088, 0),

            new data(5478, 5340, 0),
            new data(5340, 5429, 0),
            new data(5429, 5435, 0),
            new data(5435, 5602, 0),
            new data(5602, 5681, 0),
            new data(5681, 5505, 0),
            new data(5505, 5340, 0),
            new data(5340, 5235, 0),
            new data(5235, 5185, 0),

            new data(5404, 5351, 0),
            new data(5351, 5329, 0),

            new data(5435, 5351, 0),

            new data(1503, 1502, 0),

            new data(1568, 1603, 0),
            new data(1603, 1542, 0),
            new data(1542, 1148, 0),
            new data(1148, 1155, 0),
            new data(1155, 1035, 0),

            new data(2527, 2209, 0),
            new data(2209, 1542, 0),

            new data(7754, 7776, 0),
            new data(7776, 7822, 0),
            new data(7822, 7936, 0),
            new data(7936, 7980, 0),
            new data(7980, 8080, 0),
            new data(8080, 8204, 0),
            new data(8204, 8260, 0),
            new data(8260, 8322, 0),
            new data(8322, 8278, 0),
            new data(8278, 8167, 0),
            new data(8167, 8075, 0),
            new data(8075, 7754, 0),

            new data(2326, 3685, 0),
            new data(3685, 4037, 0),
            new data(4037, 4199, 0),
            new data(4199, 4140, 0),
            new data(4140, 4050, 0),
            new data(4050, 3699, 0),
            new data(3699, 3307, 0),
            new data(3307, 3117, 0),
            new data(3117, 3207, 0),

            new data(3485, 3699, 0),

            new data(4140, 4257, 0),
            new data(4257, 4337, 0),
            new data(4337, 4352, 0),
            new data(4352, 4325, 0),
            new data(4325, 4199, 0),

            new data(542, 403, 0),
            new data(403, 264, 0),
            new data(264, 168, 0),
            new data(168, 21, 0),

            new data(5459, 5132, 0),
            new data(5132, 5267, 0),

            new data(5576, 5440, 0),
            new data(5440, 5248, 0),
            new data(5248, 5249, 0),
            new data(5249, 5231, 0),
            new data(5231, 5193, 0),
            new data(5193, 5190, 0),
            new data(5190, 5288, 0),
            new data(5288, 5367, 0),
            new data(5367, 5285, 0),
            new data(5285, 5248, 0),

            new data(4390, 4618, 0),
            new data(4618, 4802, 0),
            new data(4802, 5231, 0),
            new data(5231, 5132, 0),
            new data(5132, 4819, 0),
            new data(4819, 4743, 0),
            new data(4743, 4638, 0),
            new data(4638, 4467, 0),

            new data(4889, 5028, 0),
            new data(5028, 5089, 0),
            new data(5089, 5190, 0),

            new data(4621, 4638, 0),

            new data(7850, 7957, 0),
            new data(7957, 8162, 0),
            new data(8162, 8316, 0),
            new data(8316, 8494, 0),
            new data(8494, 8465, 0),
            new data(8465, 8571, 0),
            new data(8571, 8694, 0),
            new data(8694, 8238, 0),
            new data(8238, 8162, 0),

            new data(8238, 8974, 0),
            new data(8974, 8694, 0),

            new data(804, 911, 0),
            new data(911, 896, 0),
            new data(896, 813, 0),
            new data(813, 718, 0),
            new data(718, 804, 0),
            new data(804, 779, 0),
            new data(779, 681, 0),
            new data(681, 539, 0),
            new data(539, 402, 0),
            new data(402, 334, 0),
            new data(334, 74, 0),
            new data(74, 188, 0),
            new data(188, 509, 0),
            new data(509, 539, 0),

            new data(3318, 3340, 0),
            new data(3340, 4234, 0),
            new data(4234, 4674, 0),
            new data(4674, 4174, 0),
            new data(4174, 3318, 0),

            new data(5670, 5463, 0),
            new data(5463, 5704, 0),

            new data(2827, 2693, 0),
            new data(2693, 2646, 0),
            new data(2646, 2618, 0),
            new data(2618, 2580, 0),
            new data(2580, 2429, 0),
            new data(2429, 2294, 0),
            new data(2294, 2491, 0),
            new data(2491, 2596, 0),
            new data(2596, 2574, 0),
            new data(2574, 2657, 0),
            new data(2657, 2596, 0),

            new data(2491, 2571, 0),
            new data(2571, 2653, 0),
            new data(2653, 2693, 0),

            new data(2943, 2845, 0),

            new data(3249, 3461, 0),
            new data(3461, 3572, 0),

            new data(3461, 3449, 0),
            new data(3449, 3474, 0),

            new data(2120, 2040, 0),
            new data(2040, 2106, 0),
            new data(2106, 2256, 0),
            new data(2256, 2296, 0),

            new data(2040, 1956, 0),
            new data(1956, 1862, 0),

            new data(4737, 4983, 0),
            new data(4983, 4969, 0),

            new data(7188, 7242, 0),
            new data(7242, 7259, 0),
            new data(7259, 7254, 0),
            new data(7254, 7226, 0),

            new data(5971, 5947, 0),
            new data(5947, 5889, 0),
            new data(5889, 5849, 0),
            new data(5849, 5793, 0),
            new data(5793, 5747, 0),
            new data(5747, 5778, 0),

            new data(4468, 4402, 0),
            new data(4402, 4382, 0),
            new data(4382, 4405, 0),
            new data(4405, 4343, 0),
            new data(4343, 4287, 0),
            new data(4287, 4382, 0),

            new data(4567, 4514, 0),
            new data(4514, 4405, 0),

            new data(4656, 4853, 0),

            new data(4763, 4730, 0),

            new data(4623, 4630, 0),
            new data(4630, 4662, 0),
            new data(4662, 4757, 0),
            new data(4757, 4786, 0),
            new data(4786, 4630, 0),

            new data(4915, 4785, 0),

            new data(7924, 7796, 0),
            new data(7796, 7528, 0),
            new data(7528, 7469, 0),
            new data(7469, 7420, 0),
            new data(7420, 7328, 0),

            new data(7418, 7615, 0),
            new data(7615, 7796, 0),
            new data(7796, 7949, 0),
            new data(7949, 8115, 0),

            new data(7852, 7882, 0),
            new data(7882, 7928, 0),
            new data(7928, 7948, 0),
            new data(7948, 7906, 0),
            new data(7906, 7882, 0),

            new data(1338, 1465, 0),
            new data(1465, 1674, 0),
            new data(1674, 1922, 0),
            new data(1922, 2102, 0),
            new data(2102, 2015, 0),
            new data(2015, 1922, 0),
            new data(1922, 1465, 0),

            new data(3751, 4434, 0),
            new data(4434, 4787, 0),
            new data(4787, 5291, 0),
            new data(5291, 5744, 0),
            new data(5744, 5986, 0),
            new data(5986, 6132, 0),
            new data(6132, 6396, 0),
            new data(6396, 6636, 0),
            new data(6636, 6927, 0),
            new data(6927, 7582, 0),
            new data(7582, 7310, 0),
            new data(7310, 6688, 0),
            new data(6688, 6705, 0),
            new data(6705, 6536, 0),
            new data(6536, 6555, 0),
            new data(6555, 6688, 0),

            new data(8123, 8178, 0),
            new data(8178, 8131, 0),
            new data(8131, 8097, 0),

            new data(1679, 1666, 0),
            new data(1666, 1560, 0),
            new data(1560, 1520, 0),
            new data(1520, 1463, 0),
            new data(1463, 1298, 0),
            new data(1298, 1231, 0),
            new data(1231, 1162, 0),
            new data(1162, 1136, 0),
            new data(1136, 1084, 0),
            new data(1084, 874, 0),
            new data(874, 818, 0),
            new data(818, 919, 0),
            new data(919, 1003, 0),
            new data(1003, 1088, 0),
            new data(1088, 1173, 0),
            new data(1173, 1213, 0),
            new data(1213, 1240, 0),
            new data(1240, 1453, 0),
            new data(1453, 1464, 0),
            new data(1464, 1393, 0),
            new data(1393, 1347, 0),
            new data(1347, 1195, 0),
            new data(1195, 1190, 0),
            new data(1190, 1106, 0),
            new data(1106, 1008, 0),
            new data(1008, 897, 0),
            new data(897, 794, 0),
            new data(794, 789, 0),
            new data(789, 721, 0),
            new data(721, 674, 0),
            new data(674, 566, 0),
            new data(566, 472, 0),

            new data(963, 841, 0),
            new data(841, 612, 0),

            new data(2891, 2697, 0),
            new data(2697, 2540, 0),

            new data(2985, 2905, 0),
            new data(2905, 2821, 0),
            new data(2821, 2697, 0),
            new data(2697, 2473, 0),
            new data(2473, 2343, 0),

            new data(2484, 2763, 0),
            new data(2763, 2777, 0),
            new data(2777, 2650, 0),
            new data(2650, 2421, 0),

            new data(2990, 2905, 0),
            new data(2905, 2777, 0),

            new data(2473, 2286, 0),
            new data(2286, 2216, 0),
            new data(2216, 2134, 0),

            new data(8353, 8411, 0),
            new data(8411, 8486, 0),
            new data(8486, 8556, 0),
            new data(8556, 8636, 0),
            new data(8636, 8425, 0),

            new data(8747, 8675, 0),
            new data(8675, 8636, 0),
            new data(8636, 8820, 0),
            new data(8820, 8787, 0),

            new data(6159, 6117, 0),
            new data(6117, 6095, 0),
            new data(6095, 6148, 0),
            new data(6148, 6212, 0),
            new data(6212, 6220, 0),
            new data(6220, 6418, 0),
            new data(6418, 6324, 0),
            new data(6324, 6410, 0),
            new data(6410, 6406, 0),
            new data(6406, 6148, 0),

            new data(6324, 6212, 0),

            new data(6410, 6526, 0),
            new data(6526, 6623, 0),
            new data(6623, 6703, 0),
            new data(6703, 6779, 0),

            new data(6220, 6168, 0),
            new data(6168, 6092, 0),
            new data(6092, 6023, 0),
            new data(6023, 5914, 0),

            new data(6418, 6485, 0),
            new data(6485, 6695, 0),
            new data(6695, 6588, 0),

            new data(909, 934, 0),
            new data(934, 802, 0),
            new data(802, 1326, 0),

            new data(5287, 5080, 0),
            new data(5080, 5020, 0),
            new data(5020, 4552, 0),
            new data(4552, 4450, 0),
            new data(4450, 4232, 0),
            new data(4232, 4094, 0),
            new data(4094, 3994, 0),
            new data(3994, 3970, 0),
            new data(3970, 3903, 0),
            new data(3903, 3748, 0),
            new data(3748, 3845, 0),
            new data(3845, 3665, 0),
            new data(3665, 3547, 0),
            new data(3547, 3482, 0),
            new data(3482, 3410, 0),
            new data(3410, 3418, 0),
            new data(3418, 3454, 0),
            new data(3454, 3547, 0),

            new data(591, 570, 0),
            new data(570, 705, 0),
            new data(705, 806, 0),
            new data(806, 98, 0),
            new data(98, 1208, 0),

            new data(8368, 8140, 0),
            new data(8140, 7869, 0),
            new data(7869, 7920, 0),
            new data(7920, 7986, 0),

            new data(8498, 8485, 0),
            new data(8485, 8579, 0),
            new data(8579, 8523, 0),
            new data(8523, 8572, 0),
            new data(8572, 8541, 0),
            new data(8541, 8538, 0),
            new data(8538, 8585, 0),
            new data(8585, 8572, 0),
            new data(8572, 8632, 0),
            new data(8632, 8579, 0),

            new data(3852, 3982, 0),
            new data(3982, 3975, 0),
            new data(3975, 4057, 0),
            new data(4057, 4030, 0),
            new data(4030, 3905, 0),
            new data(3905, 3873, 0),

            new data(4058, 4357, 0),
            new data(4357, 4359, 0),
            new data(4359, 3982, 0),

            new data(4357, 4534, 0),
            new data(4534, 4359, 0),

            new data(1705, 1702, 0),
            new data(1702, 1865, 0),
            new data(1865, 1829, 0),
            new data(1829, 1654, 0),
            new data(1654, 1702, 0),
            new data(1702, 1756, 0),

            new data(1865, 1998, 0),
            new data(1998, 2085, 0),
            new data(2085, 2155, 0),
            new data(2155, 2035, 0),
            new data(2035, 1983, 0),
            new data(1983, 1829, 0),

            new data(5603, 5531, 0),
            new data(5531, 5685, 0),
            new data(5685, 5787, 0),
            new data(5787, 5530, 0),

            new data(5787, 5794, 0),
            new data(5794, 5812, 0),

            new data(3800, 3974, 0),
            new data(3974, 4100, 0),
            new data(4100, 4247, 0),
            new data(4247, 4090, 0),
            new data(4090, 3974, 0),

            new data(5571, 5695, 0),
            new data(5695, 5776, 0),
            new data(5776, 5708, 0),
            new data(5708, 5646, 0),
            new data(5646, 5649, 0),
            new data(5649, 5469, 0),

            new data(5776, 5948, 0),
            new data(5948, 5649, 0),

            new data(5883, 5705, 0),
            new data(5705, 5948, 0),
            new data(5948, 5883, 0),

            new data(3705, 3690, 0),
            new data(3690, 3579, 0),
            new data(3579, 3275, 0),
            new data(3275, 2818, 0),
            new data(2818, 2560, 0),
            new data(2560, 2238, 0),

            new data(7001, 7056, 0),
            new data(7056, 7106, 0),
            new data(7106, 7178, 0),
            new data(7178, 7139, 0),
            new data(7139, 7056, 0),

            new data(2261, 1953, 0),
            new data(1953, 1629, 0),
            new data(1629, 1677, 0),

            new data(7965, 8039, 0),
            new data(8039, 8135, 0),
            new data(8135, 8151, 0),

            new data(2227, 2356, 0),
            new data(2356, 2714, 0),
            new data(2714, 3188, 0),
            new data(3188, 2970, 0),

            new data(2714, 2506, 0),
            new data(2506, 2298, 0),
            new data(2298, 2385, 0),
            new data(2385, 2506, 0),

            new data(2385, 2456, 0),

            new data(4844, 4798, 0),
            new data(4798, 4773, 0),

            new data(4923, 4798, 0),
            new data(4798, 4671, 0),
            new data(4671, 4520, 0),

            new data(5962, 6072, 0),
            new data(6072, 6115, 0),
            new data(6115, 5980, 0),
            new data(5980, 5962, 0),

            new data(8630, 8254, 0),
            new data(8254, 5339, 0),
            new data(5339, 8630, 0),

            new data(6698, 6629, 0),
            new data(6629, 6603, 0),
            new data(6603, 6378, 0),
            new data(6378, 6175, 0),
            new data(6175, 6299, 0),
            new data(6299, 6556, 0),
            new data(6556, 6771, 0),
            new data(6771, 6714, 0),
            new data(6714, 6603, 0),
            new data(6603, 6556, 0),

            new data(6299, 6149, 0),
            new data(6149, 6056, 0),
            new data(6056, 6075, 0),
            new data(6075, 6129, 0),
            new data(6129, 6175, 0),
            new data(6175, 6147, 0),
            new data(6147, 6118, 0),
            new data(6118, 6104, 0),
            new data(6104, 6112, 0),

            new data(6492, 6453, 0),
            new data(6453, 6486, 0),
            new data(6486, 6445, 0),
            new data(6445, 6378, 0),

            new data(1569, 1544, 0),
            new data(1544, 1543, 0),
            new data(1543, 1552, 0),
            new data(1552, 1567, 0),

            new data(1543, 1790, 0),
            new data(1790, 1879, 0),
            new data(1879, 2061, 0),
            new data(2061, 1948, 0),
            new data(1948, 2004, 0),
            new data(2004, 1713, 0),
            new data(1713, 1852, 0),
            new data(1852, 1790, 0),

            new data(2061, 2124, 0),
            new data(2124, 2199, 0),
            new data(2199, 2135, 0),

            new data(2047, 2159, 0),
            new data(2159, 2199, 0),

            new data(1948, 1852, 0),

            new data(6582, 6745, 0),
            new data(6745, 6855, 0),
            new data(6855, 7074, 0),
            new data(7074, 7665, 0),
            new data(7665, 7913, 0),
            new data(7913, 8181, 0),
            new data(8181, 7790, 0),
            new data(7790, 7665, 0),
            new data(7665, 7590, 0),

            new data(6982, 7665, 0),
            new data(7665, 7107, 0),
            new data(7107, 6745, 0),

            new data(8173, 8313, 0),
            new data(8313, 8667, 0),
            new data(8667, 8684, 0),
            new data(8684, 8775, 0),
            new data(8775, 8781, 0),
            new data(8781, 39, 0),
            new data(39, 15, 0),
            new data(15, 8775, 0),
            new data(8775, 8650, 0),
            new data(8650, 8430, 0),
            new data(8430, 8315, 0),

            new data(8781, 8665, 0),
            new data(8665, 8634, 0),
            new data(8634, 8450, 0),
            new data(8450, 8308, 0),

            new data(840, 921, 0),
            new data(921, 936, 0),
            new data(936, 941, 0),
            new data(941, 937, 0),
            new data(937, 1017, 0),
            new data(1017, 1122, 0),
            new data(1122, 1220, 0),
            new data(1220, 936, 0),

            new data(496, 799, 0),
            new data(799, 937, 0),
            new data(937, 854, 0),
            new data(854, 834, 0),
            new data(834, 915, 0),
            new data(915, 1017, 0),

            new data(1131, 1203, 0),
            new data(1203, 1228, 0),
            new data(1228, 1220, 0),

            new data(1122, 1273, 0),
            new data(1273, 1303, 0),
            new data(1303, 1324, 0),
            new data(1324, 1261, 0),

            new data(440, 429, 0),
            new data(429, 322, 0),
            new data(322, 99, 0),
            new data(99, 25, 0),
            new data(25, 191, 0),
            new data(191, 338, 0),
            new data(338, 322, 0),

            new data(25, 8949, 0),
            new data(8949, 8959, 0),
            new data(8959, 9069, 0),

            new data(2020, 2042, 0),
            new data(2042, 2550, 0),

            new data(8728, 8720, 0),
            new data(8720, 8695, 0),
            new data(8695, 8576, 0),
            new data(8576, 8431, 0),
            new data(8431, 8326, 0),
            new data(8326, 8305, 0),
            new data(8305, 8431, 0),
            new data(8431, 8628, 0),
            new data(8628, 8728, 0),

            new data(8969, 8916, 0),
            new data(8916, 8852, 0),
            new data(8852, 8911, 0),
            new data(8911, 8984, 0),
            new data(8984, 8969, 0),
            new data(8969, 9072, 0),
            new data(9072, 224, 0),
            new data(224, 294, 0),
            new data(294, 434, 0),
            new data(434, 489, 0),
            new data(489, 596, 0),
            new data(596, 510, 0),
            new data(510, 437, 0),
            new data(437, 360, 0),
            new data(360, 383, 0),
            new data(383, 352, 0),
            new data(352, 360, 0),

            new data(3207, 3165, 0),
            new data(3165, 3185, 0),
            new data(3185, 3045, 0),
            new data(3045, 2948, 0),
            new data(2948, 2922, 0),
            new data(2922, 2996, 0),
            new data(2996, 3045, 0),

            new data(2922, 2773, 0),
            new data(2773, 2451, 0),
            new data(2451, 2326, 0),

            new data(3518, 3468, 0),
            new data(3468, 3438, 0),

            new data(1336, 1175, 0),
            new data(1175, 1247, 0),
            new data(1247, 1355, 0),
            new data(1355, 1336, 0),

            new data(8937, 8863, 0),
            new data(8863, 9016, 0),
            new data(9016, 280, 0),

            new data(5928, 5944, 0),
            new data(5944, 5953, 0),
            new data(5953, 5985, 0),
            new data(5985, 6027, 0),

            new data(6580, 6508, 0),
            new data(6508, 6527, 0),
            new data(6527, 6630, 0),
            new data(6630, 6615, 0),
            new data(6615, 6558, 0),
            new data(6558, 6380, 0),
            new data(6380, 6271, 0),
            new data(6271, 6252, 0),
            new data(6252, 6241, 0),
            new data(6241, 6165, 0),
            new data(6165, 6134, 0),
            new data(6134, 6084, 0),
            new data(6084, 5944, 0),

            new data(6084, 5993, 0),
            new data(5993, 5984, 0),

            new data(7063, 6973, 0),
            new data(6973, 6930, 0),
            new data(6930, 7020, 0),
            new data(7020, 7063, 0),

            new data(6056, 5881, 0),
            new data(5881, 5892, 0),
            new data(5892, 5854, 0),
            new data(5854, 5789, 0),
            new data(5789, 5867, 0),
            new data(5867, 5933, 0),
            new data(5933, 5879, 0),
            new data(5879, 5842, 0),
            new data(5842, 5867, 0),

            new data(7141, 6869, 0),
            new data(6869, 6698, 0),
            new data(6698, 6561, 0),
            new data(6561, 6378, 0),

            new data(3909, 3981, 0),
            new data(3981, 4119, 0),

            new data(7635, 7536, 0),
            new data(7536, 7479, 0),

            new data(7536, 7488, 0),

            new data(6812, 6913, 0),
            new data(6913, 6859, 0),
            new data(6859, 7039, 0),
            new data(7039, 7194, 0),
            new data(7194, 7234, 0),
            new data(7234, 7121, 0),
            new data(7121, 7039, 0),
            new data(7039, 6913, 0),

            new data(7194, 6879, 0),
            new data(6879, 6859, 0),
            new data(6859, 6746, 0),
            new data(6746, 6879, 0),
            new data(6879, 6832, 0),

            new data(1910, 1457, 0),
            new data(1457, 1412, 0),
            new data(1412, 1346, 0),
            new data(1346, 1239, 0),
            new data(1239, 1066, 0),
            new data(1066, 1038, 0),
            new data(1038, 1030, 0),

            new data(1791, 1497, 0),
            new data(1497, 1409, 0),
            new data(1409, 1373, 0),
            new data(1373, 1346, 0),

            new data(1239, 1320, 0),
            new data(1320, 1251, 0),

            new data(6905, 6897, 0),
            new data(6897, 6783, 0),

            new data(5897, 5671, 0),
            new data(5671, 6217, 0),
            new data(6217, 5897, 0),

            new data(664, 622, 0),
            new data(622, 544, 0),
            new data(544, 664, 0),

            new data(8502, 8540, 0),
            new data(8540, 9076, 0),
            new data(9076, 77, 0),
            new data(77, 126, 0),
            new data(126, 8848, 0),
            new data(8848, 8502, 0),

            new data(5191, 5054, 0),
            new data(5054, 4905, 0),
            new data(4905, 4660, 0),
            new data(4660, 4301, 0),
            new data(4301, 3757, 0),
            new data(3757, 3323, 0),
            new data(3323, 3888, 0),
            new data(3888, 3757, 0),

            new data(4375, 4377, 0),
            new data(4377, 4518, 0),
            new data(4518, 4554, 0),
            new data(4554, 4660, 0),

            new data(4033, 4069, 0),
            new data(4069, 4335, 0),
            new data(4335, 4518, 0),

            new data(4554, 4295, 0),
            new data(4295, 3888, 0),
            new data(3888, 3775, 0),
            new data(3775, 3594, 0),
            new data(3594, 3569, 0),

            new data(4301, 4295, 0),

            new data(424, 6789, 0),
            new data(6789, 6322, 0),
            new data(6322, 5903, 0),
            new data(5903, 5563, 0),
            new data(5563, 5735, 0),
            new data(5735, 6116, 0),
            new data(6116, 5903, 0),

            new data(3207, 3485, 0),
            new data(3485, 3734, 0),
            new data(3734, 3940, 0),
            new data(3940, 4216, 0),
            new data(4216, 4023, 0),
            new data(4023, 3786, 0),
            new data(3786, 3634, 0),
            new data(3634, 3207, 0),

            new data(4932, 4910, 0),
            new data(4910, 4825, 0),
            new data(4825, 5056, 0),
            new data(5056, 5107, 0),
            new data(5107, 4910, 0),

            new data(5107, 5264, 0),
            new data(5264, 5511, 0),

            new data(5487, 5338, 0),
            new data(5338, 5264, 0),

            new data(5338, 5056, 0),

            new data(4517, 4540, 0),
            new data(4540, 4689, 0),
            new data(4689, 4825, 0),
            new data(4825, 4608, 0),
            new data(4608, 4517, 0),

            new data(3615, 3347, 0),
            new data(3347, 3223, 0),
            new data(3223, 2803, 0),
            new data(2803, 2736, 0),
            new data(2736, 3024, 0),
            new data(3024, 3223, 0),

            new data(7306, 7405, 0),
            new data(7405, 7592, 0)};


    public static ConNamePoint[] connames = new ConNamePoint[]{

            new ConNamePoint(0.5, 40, 1),
            new ConNamePoint(10, -35, 2),
            new ConNamePoint(16, -75, 3),
            new ConNamePoint(23, -15, 4),
            new ConNamePoint(19.5, 5, 5),
            new ConNamePoint(17.5, -55, 6),
            new ConNamePoint(2.5, 20, 7),
            new ConNamePoint(6, 40, 8),
            new ConNamePoint(14.5, 30, 9),
            new ConNamePoint(4.5, 40, 10),
            new ConNamePoint(5.5, 70, 11),
            new ConNamePoint(8.5, 20, 12),
            new ConNamePoint(13, 40, 13),
            new ConNamePoint(7, -20, 14),
            new ConNamePoint(7.5, 5, 15),
            new ConNamePoint(21, -20, 16),
            new ConNamePoint(9, -60, 17),
            new ConNamePoint(1, 60, 18),
            new ConNamePoint(13, -55, 19),
            new ConNamePoint(22, 65, 20),
            new ConNamePoint(1.5, -10, 21),
            new ConNamePoint(10.5, 80, 22), //check
            new ConNamePoint(14.5, -65, 23),
            new ConNamePoint(5.5, -35, 24),
            new ConNamePoint(13, 20, 25),
            new ConNamePoint(18.5, -40, 26),
            new ConNamePoint(16, 30, 27),
    };


    //for quick drawing we calculate some figures for a given time
    //if time is changed we need to recalculate, thus we raise this recalculation flag
    public static void raiseNewPointFlag() {
        if (Global.cf != null) //raising flag on points representing constellations
            for (ConPoint cp : Global.cf)
                cp.raiseNewPointFlag();
    }

    //setting constellations

    /* idea is as follows: we break a sky into quadrants (ra1,ra2,dec1,dec2) and then
     * calculate to which quadrant each con point belongs.  This is used in quick redrawing in CustomView -
     * only point belonging to the quadrants that needs to be depicted are drawn
     * TO SPEED UP INITIAL INITIALIZATION - MAKE A FILE WITH A LIST OF WHAT CON POINT BELONGS TO EACH QUADRANT (AS IS DONE WITH STAR DATABASES
     */
    public static void setConFigures() {
        Global.cf = new ArrayList<ConPoint>();
        Global.cflist = new MyList[Quadrant.quadrants.length];
        for (int k = 0; k < Global.cflist.length; k++)
            Global.cflist[k] = new MyList();
        int i = 0;
        for (data d : list) {
            ConPoint cp = new ConPoint(d.hrstart, d.hrend);
            Global.cf.add(cp);
            int l = 0;
            int m = -1;
            for (Quadrant q : Quadrant.quadrants) {
                if (q.inQuadrant(cp.ra, cp.dec)) {
                    m = l;
                    break;
                }
                l++;

            }
            if (m >= 0)
                Global.cflist[m].add(i);
            else
                Log.d(TAG, "conpoint without quadrant" + cp.getHrs());
            i++;
        }
    }


    /**
     * filling Global.cf only.  Filling Global.cflist is now done from the file
     */
    public static void fillConstellationList() {
        Global.cf = new ArrayList<ConPoint>();

        int i = 0;
        for (data d : list) {
            ConPoint cp = new ConPoint(d.hrstart, d.hrend);
            Global.cf.add(cp);

        }
    }
}
