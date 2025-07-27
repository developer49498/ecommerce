import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  FlatList,
  Image,
  TouchableOpacity,
  Platform,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';

const tripHistory = [
  {
    id: '1',
    from: 'Mumbai',
    to: 'Pune',
    driver: 'Ravi Kumar',
    vehicle: 'MH12AB1234',
    date: '2025-06-10',
    status: 'Confirmed',
  },
  {
    id: '2',
    from: 'Delhi',
    to: 'Agra',
    driver: 'Amit Sharma',
    vehicle: 'DL8CAF0987',
    date: '2025-06-08',
    status: 'Completed',
  },
  {
    id: '3',
    from: 'Bangalore',
    to: 'Mysore',
    driver: 'Sneha Reddy',
    vehicle: 'KA01GH5678',
    date: '2025-06-05',
    status: 'Pending',
  },
  {
    id: '4',
    from: 'Chennai',
    to: 'Madurai',
    driver: 'Arjun Das',
    vehicle: 'TN10JK3456',
    date: '2025-06-02',
    status: 'Rejected',
  },
  {
    id: '5',
    from: 'Kolkata',
    to: 'Digha',
    driver: 'Priya Singh',
    vehicle: 'WB02XY9876',
    date: '2025-05-30',
    status: 'Completed',
  },
  {
    id: '6',
    from: 'Hyderabad',
    to: 'Warangal',
    driver: 'Suresh Rao',
    vehicle: 'TS07AZ1122',
    date: '2025-05-28',
    status: 'Confirmed',
  },
  {
    id: '7',
    from: 'Jaipur',
    to: 'Udaipur',
    driver: 'Geeta Devi',
    vehicle: 'RJ14BC3344',
    date: '2025-05-25',
    status: 'Pending',
  },
];

const statusColors = {
  Confirmed: { background: '#e0ffe0', text: '#28A745' },
  Completed: { background: '#e0f0ff', text: '#007BFF' },
  Pending: { background: '#fffbe0', text: '#FFC107' },
  Rejected: { background: '#ffe0e0', text: '#DC3545' },
};

export default function HistoryScreen({ navigation }) {
  const [activeTab, setActiveTab] = useState('History');
  const insets = useSafeAreaInsets();

  const handleTabPress = (tabName) => {
    setActiveTab(tabName);
    if (navigation && navigation.getState().routes[navigation.getState().index].name !== tabName) {
      navigation.navigate(tabName);
    }
  };

  const renderItem = ({ item }) => {
    const statusStyle = statusColors[item.status] || { background: 'rgba(255,255,255,0.1)', text: '#BDC3C7' };

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.locationContainer}>
            <Icon name="location-on" size={18} color="#FF7518" style={styles.locationIcon} />
            <Text style={styles.locationText}>{item.from}</Text>
            <Icon name="arrow-forward" size={18} color="#95A5A6" style={styles.arrowIcon} />
            <Text style={styles.locationText}>{item.to}</Text>
          </View>
          <View style={[styles.statusTag, { backgroundColor: statusStyle.background }]}>
            <Text style={[styles.statusText, { color: statusStyle.text }]}>
              {item.status.toUpperCase()}
            </Text>
          </View>
        </View>

        <View style={styles.detailRow}>
          <MaterialCommunityIcons name="account" size={16} color="#95A5A6" style={styles.detailIcon} />
          <Text style={styles.detailLabel}>Driver:</Text>
          <Text style={styles.detailValue}>{item.driver}</Text>
        </View>
        <View style={styles.detailRow}>
          <MaterialCommunityIcons name="car" size={16} color="#95A5A6" style={styles.detailIcon} />
          <Text style={styles.detailLabel}>Vehicle:</Text>
          <Text style={styles.detailValue}>{item.vehicle}</Text>
        </View>
        <View style={styles.detailRow}>
          <Icon name="calendar-today" size={16} color="#95A5A6" style={styles.detailIcon} />
          <Text style={styles.detailLabel}>Date:</Text>
          <Text style={styles.detailValue}>{item.date}</Text>
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView style={[styles.container, { paddingTop: Platform.OS === 'android' ? insets.top : 0 }]}>
      <View style={styles.screenHeader}>
        <Text style={styles.screenTitle}>Trip History</Text>
      </View>

      <FlatList
        data={tripHistory}
        renderItem={renderItem}
        keyExtractor={(item) => item.id + item.date + item.from}
        contentContainerStyle={styles.listContentContainer}
        showsVerticalScrollIndicator={false}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1A24',
  },
  screenHeader: {
    paddingVertical: 20,
    paddingHorizontal: 20,
    backgroundColor: '#0F1A24',
    alignItems: 'center',
    justifyContent: 'center',
  },
  screenTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ECF0F1',
  },
  listContentContainer: {
    paddingHorizontal: 16,
    paddingBottom: 20,
  },
  card: {
    // --- MODIFIED CARD BACKGROUND COLOR ---
    backgroundColor: '#203040', // A dark, subtle blue
    borderRadius: 15,
    padding: 18,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25,
    shadowRadius: 10,
    elevation: 10,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
    paddingBottom: 10,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.1)',
  },
  locationContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    flexShrink: 1,
  },
  locationIcon: {
    marginRight: 5,
  },
  locationText: {
    fontSize: 17,
    fontWeight: '600',
    color: '#ECF0F1',
    flexShrink: 1,
  },
  arrowIcon: {
    marginHorizontal: 8,
  },
  statusTag: {
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 20,
    width: 100,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statusText: {
    fontSize: 12,
    fontWeight: 'bold',
    textTransform: 'uppercase',
    textAlign: 'center',
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  detailIcon: {
    marginRight: 10,
    opacity: 0.7,
  },
  detailLabel: {
    fontSize: 14,
    color: '#95A5A6',
    marginRight: 5,
    fontWeight: '500',
  },
  detailValue: {
    fontSize: 14,
    color: '#ECF0F1',
    flex: 1,
  },
});