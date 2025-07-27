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
  Linking,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { useTripRequests } from './TripRequestStore';

const allTrips = [
  {
    id: 'req1',
    from: 'Koramangala',
    to: 'Indiranagar',
    customer: 'Ananya Sharma',
    phone: '9876543210',
    date: '2025-06-12',
    status: 'Pending',
  },
  {
    id: 'req2',
    from: 'Jayanagar',
    to: 'Whitefield',
    customer: 'Rohan Mehta',
    phone: '9123456780',
    date: '2025-06-12',
    status: 'Pending',
  },
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
    id: '4',
    from: 'Chennai',
    to: 'Madurai',
    driver: 'Arjun Das',
    vehicle: 'TN10JK3456',
    date: '2025-06-02',
    status: 'Rejected',
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
];

const tripRequests = allTrips.filter(trip => trip.status === 'Pending');
const tripHistory = allTrips.filter(trip => trip.status !== 'Pending');

const statusColors = {
  Confirmed: { background: '#e0ffe0', text: '#28A745' },
  Completed: { background: '#e0f0ff', text: '#007BFF' },
  Pending: { background: '#fffbe0', text: '#FFC107' },
  Rejected: { background: '#ffe0e0', text: '#DC3545' },
};

export default function DriverMyTripsScreen({ navigation }) {
  const [activeTab, setActiveTab] = useState('Requests');
  const insets = useSafeAreaInsets();
  const { tripRequests } = useTripRequests();

  const handleTabPress = (tabName) => {
    setActiveTab(tabName);
    if (navigation && navigation.getState().routes[navigation.getState().index].name !== tabName) {
      navigation.navigate(tabName);
    }
  };

  const handleContact = (phoneNumber) => {
    Linking.openURL(`tel:${phoneNumber}`);
  };

  const renderTripRequest = ({ item }) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.locationContainer}>
          <Icon name="location-on" size={18} color="#FF7518" style={styles.locationIcon} />
          <Text style={styles.locationText}>{item.from}</Text>
          <Icon name="arrow-forward" size={18} color="#95A5A6" style={styles.arrowIcon} />
          <Text style={styles.locationText}>{item.to}</Text>
        </View>
      </View>
      <View style={styles.detailRow}>
        <MaterialCommunityIcons name="account" size={16} color="#95A5A6" style={styles.detailIcon} />
        <Text style={styles.detailLabel}>Customer:</Text>
        <Text style={styles.detailValue}>{item.customer}</Text>
      </View>
      <View style={styles.detailRow}>
        <Icon name="calendar-today" size={16} color="#95A5A6" style={styles.detailIcon} />
        <Text style={styles.detailLabel}>Date:</Text>
        <Text style={styles.detailValue}>{item.date}</Text>
      </View>
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={[styles.actionButton, styles.declineButton]}>
          <Text style={styles.actionButtonText}>Decline</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.actionButton, styles.contactButton]} onPress={() => handleContact(item.phone)}>
          <Text style={[styles.actionButtonText, { color: '#fff' }]}>Contact</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.actionButton, styles.acceptButton]}>
          <Text style={[styles.actionButtonText, { color: '#fff' }]}>Accept</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderTripHistory = ({ item }) => {
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
        <Text style={styles.screenTitle}>My Trips</Text>
      </View>
      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'Requests' && styles.activeTab]}
          onPress={() => setActiveTab('Requests')}
        >
          <Text style={[styles.tabText, activeTab === 'Requests' && styles.activeTabText]}>Trip Requests</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'History' && styles.activeTab]}
          onPress={() => setActiveTab('History')}
        >
          <Text style={[styles.tabText, activeTab === 'History' && styles.activeTabText]}>Trip History</Text>
        </TouchableOpacity>
      </View>
      {activeTab === 'Requests' ? (
        <FlatList
          data={tripRequests}
          renderItem={renderTripRequest}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContentContainer}
        />
      ) : (
        <FlatList
          data={tripHistory}
          renderItem={renderTripHistory}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContentContainer}
        />
      )}
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
   
    backgroundColor: '#203040', 
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
    width: 90,
    paddingVertical: 4,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
  },
  statusText: {
    fontSize: 11,
    fontWeight: 'bold',
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
  },
  detailIcon: {
    marginRight: 8,
  },
  detailLabel: {
    fontSize: 15,
    color: '#95A5A6',
    marginRight: 5,
    fontWeight: '500',
  },
  detailValue: {
    fontSize: 15,
    color: '#ECF0F1',
    flexShrink: 1,
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#1c2a3a',
    marginHorizontal: 16,
    borderRadius: 10,
    marginTop: 10,
    marginBottom: 10,
    overflow: 'hidden',
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
  },
  activeTab: {
    backgroundColor: '#FF7518',
  },
  tabText: {
    color: '#ECF0F1',
    fontWeight: '600',
  },
  activeTabText: {
    color: '#fff',
  },
  fareText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#28A745',
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 15,
  },
  actionButton: {
    paddingVertical: 8,
    paddingHorizontal: 18,
    borderRadius: 20,
    marginLeft: 10,
  },
  declineButton: {
    backgroundColor: '#3E4A5C',
  },
  contactButton: {
    backgroundColor: '#17a2b8',
  },
  acceptButton: {
    backgroundColor: '#28A745',
  },
  actionButtonText: {
    color: '#ECF0F1',
    fontWeight: 'bold',
  },
}); 